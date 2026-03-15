package com.nexvault.wallet.domain.repository

import com.nexvault.wallet.domain.model.chain.Chain
import com.nexvault.wallet.domain.model.common.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for blockchain network management.
 */
interface ChainRepository {
    /**
     * Get all supported chains.
     */
    fun getSupportedChains(): Flow<List<Chain>>

    /**
     * Get only visible chains (user may hide some).
     */
    fun getVisibleChains(): Flow<List<Chain>>

    /**
     * Get the currently selected chain.
     */
    fun getSelectedChain(): Flow<Chain>

    /**
     * Set the selected chain by chain ID.
     */
    suspend fun setSelectedChain(chainId: Int): DataResult<Unit>

    /**
     * Get a chain by its ID.
     */
    fun getChainById(chainId: Int): Chain?

    /**
     * Toggle visibility of a chain in the selector.
     */
    suspend fun setChainVisible(chainId: Int, visible: Boolean): DataResult<Unit>
}
