package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.token.Token
import com.nexvault.wallet.domain.model.transaction.Transaction
import com.nexvault.wallet.domain.repository.TransactionRepository
import com.nexvault.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Returns recent transactions for the active account filtered to a single token.
 *
 * @param chainId Chain ID.
 * @param tokenContractAddress Native sentinel [Token.NATIVE_TOKEN_ADDRESS] or ERC-20 contract address.
 * @param limit Maximum number of rows.
 */
class GetRecentTokenTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(
        chainId: Int,
        tokenContractAddress: String,
        limit: Int = 5,
    ): List<Transaction> {
        val wallet = walletRepository.getActiveWallet().first() ?: return emptyList()
        val address = wallet.accounts.find { it.isActive }?.address ?: return emptyList()
        val contractFilter = if (tokenContractAddress == Token.NATIVE_TOKEN_ADDRESS) {
            null
        } else {
            tokenContractAddress.lowercase()
        }
        return transactionRepository.getRecentTransactionsForToken(
            chainId = chainId,
            address = address,
            tokenContractAddress = contractFilter,
            limit = limit,
        )
    }
}
