package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.repository.TokenRepository
import javax.inject.Inject

/**
 * Loads CoinGecko historical prices for a specific token (by chain and contract address).
 *
 * @param chainId Chain ID.
 * @param contractAddress Token contract address or native sentinel.
 * @param days Number of days of history (mapped to CoinGecko API as in [TokenRepository.getTokenPriceChart]).
 */
class GetTokenPriceChartUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(
        chainId: Int,
        contractAddress: String,
        days: Int,
    ): DataResult<List<PricePoint>> {
        return tokenRepository.getTokenPriceChart(chainId, contractAddress, days)
    }
}
