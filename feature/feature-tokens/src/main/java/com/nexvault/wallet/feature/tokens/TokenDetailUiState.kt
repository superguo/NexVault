package com.nexvault.wallet.feature.tokens

import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.model.token.Token
import com.nexvault.wallet.domain.model.transaction.Transaction

/**
 * UI state for the token detail screen.
 *
 * @param token Current token row from Room, or null if missing.
 * @param chartData Price history points for the chart.
 * @param selectedChartDays Selected range in days (1, 7, 30, or 365).
 * @param isChartLoading Whether the chart request is in flight.
 * @param recentTransactions Recent transactions for this token.
 * @param isLoading Initial token observation not yet completed.
 * @param isRefreshing Pull-to-refresh in progress.
 * @param error Optional user-visible error when the token cannot be shown.
 */
data class TokenDetailUiState(
    val token: Token? = null,
    val chartData: List<PricePoint> = emptyList(),
    val selectedChartDays: Int = 7,
    val isChartLoading: Boolean = false,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
