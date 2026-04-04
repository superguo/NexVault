package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.common.WalletNotFoundException
import com.nexvault.wallet.domain.model.token.Portfolio
import com.nexvault.wallet.domain.model.token.Token
import com.nexvault.wallet.domain.repository.ChainRepository
import com.nexvault.wallet.domain.repository.TokenRepository
import com.nexvault.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Combines token balances and prices into a [Portfolio] for the Home screen.
 * Chart series are loaded separately via [GetPriceHistoryUseCase].
 */
class GetPortfolioUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val walletRepository: WalletRepository,
    private val chainRepository: ChainRepository,
) {
    operator fun invoke(): Flow<DataResult<Portfolio>> {
        return combine(
            walletRepository.getActiveAddress(),
            chainRepository.getSelectedChain(),
        ) { address, chain ->
            address to chain
        }.flatMapLatest { (address, chain) ->
            if (address == null) {
                flowOf(DataResult.Error(WalletNotFoundException()))
            } else {
                tokenRepository.getTokensWithBalances(chain.chainId, address).map { tokens ->
                    val total = tokens.sumOf { it.fiatValue ?: 0.0 }
                    val change = weightedChange24h(tokens, total)
                    DataResult.Success(
                        Portfolio(
                            totalFiatValue = total,
                            change24hPercent = change,
                            tokens = tokens,
                            chartData = emptyList(),
                        ),
                    )
                }
            }
        }
    }

    private fun weightedChange24h(tokens: List<Token>, totalValue: Double): Double {
        if (totalValue == 0.0) return 0.0
        return tokens.sumOf { token ->
            val weight = (token.fiatValue ?: 0.0) / totalValue
            weight * (token.priceChange24h ?: 0.0)
        }
    }
}
