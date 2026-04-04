package com.nexvault.wallet.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.usecase.chain.GetSelectedChainUseCase
import com.nexvault.wallet.domain.usecase.chain.GetSupportedChainsUseCase
import com.nexvault.wallet.domain.usecase.chain.SetSelectedChainUseCase
import com.nexvault.wallet.domain.usecase.token.AddCustomTokenUseCase
import com.nexvault.wallet.domain.usecase.token.GetPortfolioUseCase
import com.nexvault.wallet.domain.usecase.token.GetPriceHistoryUseCase
import com.nexvault.wallet.domain.usecase.token.RefreshBalancesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for the Home dashboard: portfolio, chart, chain selection, and custom tokens.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val refreshBalancesUseCase: RefreshBalancesUseCase,
    private val addCustomTokenUseCase: AddCustomTokenUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val getSupportedChainsUseCase: GetSupportedChainsUseCase,
    private val getSelectedChainUseCase: GetSelectedChainUseCase,
    private val setSelectedChainUseCase: SetSelectedChainUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val chartDays = MutableStateFlow(7)

    init {
        observePortfolio()
        observeChains()
        loadInitialData()
    }

    private fun observePortfolio() {
        viewModelScope.launch {
            getPortfolioUseCase().collect { result ->
                when (result) {
                    is DataResult.Success -> {
                        val p = result.data
                        _uiState.update { state ->
                            state.copy(
                                totalFiatValue = p.totalFiatValue,
                                change24hPercent = p.change24hPercent,
                                tokens = p.tokens,
                                isLoading = false,
                            )
                        }
                    }
                    is DataResult.Error -> {
                        _uiState.update {
                            it.copy(
                                totalFiatValue = 0.0,
                                change24hPercent = 0.0,
                                tokens = emptyList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeChains() {
        viewModelScope.launch {
            combine(
                getSupportedChainsUseCase(),
                getSelectedChainUseCase(),
            ) { supported, selected ->
                supported to selected
            }.collect { (supported, selected) ->
                _uiState.update { state ->
                    state.copy(
                        supportedChains = supported,
                        selectedChain = selected,
                    )
                }
                loadChartData()
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = refreshBalancesUseCase()) {
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load balances. Pull to refresh.",
                        )
                    }
                }
                is DataResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                when (val result = refreshBalancesUseCase()) {
                    is DataResult.Error -> {
                        val msg = when (result.exception) {
                            is IOException -> "Network error. Please check your connection."
                            else -> "Failed to refresh. Please try again."
                        }
                        _uiState.update { it.copy(error = msg) }
                    }
                    is DataResult.Success -> { }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(error = "Network error. Please check your connection.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to refresh. Please try again.") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun onChainSelected(chainId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            when (setSelectedChainUseCase(chainId)) {
                is DataResult.Error -> { }
                is DataResult.Success -> {
                    refreshBalancesUseCase()
                    loadChartData()
                }
            }
        }
    }

    fun onChartRangeSelected(days: Int) {
        chartDays.value = days
        loadChartData()
    }

    private fun loadChartData() {
        viewModelScope.launch {
            val chain = _uiState.value.selectedChain ?: return@launch
            val days = chartDays.value
            when (val result = getPriceHistoryUseCase(chain.chainId, days)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            chartData = result.data,
                            selectedChartDays = days,
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(chartData = emptyList(), selectedChartDays = days) }
                }
            }
        }
    }

    fun onAddCustomToken(contractAddress: String) {
        viewModelScope.launch {
            val chainId = _uiState.value.selectedChain?.chainId ?: return@launch
            _uiState.update { it.copy(addTokenLoading = true, addTokenError = null) }
            when (val result = addCustomTokenUseCase(chainId, contractAddress)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            addTokenLoading = false,
                            showAddTokenDialog = false,
                            addTokenResult = result.data,
                        )
                    }
                    refreshBalancesUseCase()
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            addTokenLoading = false,
                            addTokenError = "Invalid contract address or not an ERC-20 token.",
                        )
                    }
                }
            }
        }
    }

    fun onShowAddTokenDialog() {
        _uiState.update {
            it.copy(showAddTokenDialog = true, addTokenError = null, addTokenResult = null)
        }
    }

    fun onDismissAddTokenDialog() {
        _uiState.update { it.copy(showAddTokenDialog = false, addTokenError = null) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }
}
