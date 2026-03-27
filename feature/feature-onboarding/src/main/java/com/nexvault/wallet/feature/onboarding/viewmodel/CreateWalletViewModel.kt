package com.nexvault.wallet.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexvault.wallet.domain.model.auth.WalletCreationResult
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.usecase.wallet.CreateWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Create Wallet screen.
 *
 * On initialization, calls CreateWalletUseCase to generate a new wallet.
 * The mnemonic words are held in state for display.
 * User must acknowledge (checkbox) before proceeding to verification.
 */
@HiltViewModel
class CreateWalletViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase,
) : ViewModel() {

    data class UiState(
        val mnemonicWords: List<String> = emptyList(),
        val walletId: String = "",
        val address: String = "",
        val isAcknowledged: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null,
    )

    sealed interface NavigationEvent {
        data class NavigateToVerifyMnemonic(val walletId: String) : NavigationEvent
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        createWallet()
    }

    private fun createWallet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = createWalletUseCase("Main Wallet")

            when (result) {
                is DataResult.Success -> {
                    val creationResult = result.data
                    _uiState.update {
                        it.copy(
                            mnemonicWords = creationResult.mnemonicWords,
                            walletId = creationResult.walletId,
                            address = creationResult.address,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to create wallet",
                        )
                    }
                }
            }
        }
    }

    fun onAcknowledgeToggled(acknowledged: Boolean) {
        _uiState.update { it.copy(isAcknowledged = acknowledged) }
    }

    fun onContinueClicked() {
        val walletId = _uiState.value.walletId
        if (walletId.isNotEmpty()) {
            _navigationEvent.tryEmit(NavigationEvent.NavigateToVerifyMnemonic(walletId))
        }
    }

    fun onRetryClicked() {
        createWallet()
    }
}
