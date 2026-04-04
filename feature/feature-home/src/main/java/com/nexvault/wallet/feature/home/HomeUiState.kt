package com.nexvault.wallet.feature.home

import com.nexvault.wallet.domain.model.chain.Chain
import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.model.token.Token

/**
 * UI state for the Home dashboard.
 */
data class HomeUiState(
    val totalFiatValue: Double = 0.0,
    val change24hPercent: Double = 0.0,
    val tokens: List<Token> = emptyList(),
    val chartData: List<PricePoint> = emptyList(),
    val selectedChartDays: Int = 7,
    val selectedChain: Chain? = null,
    val supportedChains: List<Chain> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showAddTokenDialog: Boolean = false,
    val addTokenLoading: Boolean = false,
    val addTokenError: String? = null,
    val addTokenResult: Token? = null,
)
