package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.repository.TokenRepository
import javax.inject.Inject

/**
 * Loads historical fiat prices for the portfolio chart (native coin for the selected chain).
 */
class GetPriceHistoryUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(
        chainId: Int,
        days: Int,
    ): DataResult<List<PricePoint>> {
        return tokenRepository.getPortfolioChartData(chainId, days)
    }
}
