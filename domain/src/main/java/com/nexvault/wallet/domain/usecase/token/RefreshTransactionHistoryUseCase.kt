package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.repository.TransactionRepository
import com.nexvault.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Refreshes cached explorer transactions for the active wallet on a chain.
 */
class RefreshTransactionHistoryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {
    /**
     * @param chainId Chain to refresh.
     */
    suspend operator fun invoke(chainId: Int) {
        val wallet = walletRepository.getActiveWallet().first() ?: return
        val address = wallet.accounts.find { it.isActive }?.address ?: return
        transactionRepository.refreshTransactionHistory(chainId, address)
    }
}
