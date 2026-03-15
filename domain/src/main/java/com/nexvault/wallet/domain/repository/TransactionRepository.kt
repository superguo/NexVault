package com.nexvault.wallet.domain.repository

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.transaction.GasEstimate
import com.nexvault.wallet.domain.model.transaction.SendTransactionParams
import com.nexvault.wallet.domain.model.transaction.Transaction
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

/**
 * Repository for transaction operations (send, history, status).
 */
interface TransactionRepository {
    /**
     * Estimate gas for a transaction.
     * Returns slow, normal, and fast options.
     */
    suspend fun estimateGas(
        fromAddress: String,
        toAddress: String,
        value: BigInteger,
        data: String?,
        chainId: Int,
    ): DataResult<GasEstimate>

    /**
     * Send a native coin transaction (ETH, BNB, MATIC).
     * Returns the transaction hash.
     */
    suspend fun sendNativeTransaction(
        params: SendTransactionParams,
        walletId: String,
        accountIndex: Int,
    ): DataResult<String>

    /**
     * Send an ERC-20 token transfer.
     * Returns the transaction hash.
     */
    suspend fun sendTokenTransaction(
        params: SendTransactionParams,
        walletId: String,
        accountIndex: Int,
    ): DataResult<String>

    /**
     * Get transaction history for an address on a chain.
     * Paginated — returns a page of transactions.
     */
    fun getTransactionHistory(
        chainId: Int,
        address: String,
        page: Int,
        pageSize: Int,
    ): Flow<List<Transaction>>

    /**
     * Force refresh transaction history from the block explorer API.
     */
    suspend fun refreshTransactionHistory(
        chainId: Int,
        address: String,
    ): DataResult<Unit>

    /**
     * Get a single transaction's full details.
     */
    suspend fun getTransactionDetail(
        txHash: String,
        chainId: Int,
    ): DataResult<Transaction>

    /**
     * Get pending transactions that need status polling.
     */
    fun getPendingTransactions(chainId: Int, address: String): Flow<List<Transaction>>

    /**
     * Check and update the status of a pending transaction.
     * Returns updated transaction.
     */
    suspend fun updateTransactionStatus(
        txHash: String,
        chainId: Int,
    ): DataResult<Transaction>
}
