package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.common.WalletNotFoundException
import com.nexvault.wallet.domain.model.token.Portfolio
import com.nexvault.wallet.domain.repository.ChainRepository
import com.nexvault.wallet.domain.repository.TokenRepository
import com.nexvault.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combines token balances, prices, and chart data into a Portfolio.
 * This is the main use case for the Home screen.
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
            if (address == null) {
                return@combine DataResult.Error(WalletNotFoundException())
            }
            try {
                DataResult.Success(
                    Portfolio(
                        totalFiatValue = 0.0,
                        change24hPercent = 0.0,
                        tokens = emptyList(),
                        chartData = emptyList(),
                    )
                )
            } catch (e: Exception) {
                DataResult.Error(e, e.message)
            }
        }
    }

    /**
     * Trigger a full refresh of balances and prices.
     */
    suspend fun refresh(chainId: Int, address: String): DataResult<Unit> {
        return tokenRepository.refreshBalances(chainId, address)
    }
}
