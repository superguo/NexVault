package com.nexvault.wallet.domain.repository

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.model.token.Token
import kotlinx.coroutines.flow.Flow

/**
 * Repository for token balance and price operations.
 */
interface TokenRepository {
    /**
     * Get all tokens with balances for a chain and address.
     * Returns cached data immediately via Flow, refreshes in background.
     */
    fun getTokensWithBalances(chainId: Int, address: String): Flow<List<Token>>

    /**
     * Force refresh all token balances from the blockchain and prices from API.
     */
    suspend fun refreshBalances(chainId: Int, address: String): DataResult<Unit>

    /**
     * Add a custom ERC-20 token by contract address.
     * Fetches name, symbol, decimals from chain for confirmation.
     */
    suspend fun addCustomToken(chainId: Int, contractAddress: String): DataResult<Token>

    /**
     * Remove a token from the tracked list.
     */
    suspend fun removeToken(chainId: Int, contractAddress: String): DataResult<Unit>

    /**
     * Get total portfolio value in fiat for a chain.
     */
    fun getTotalFiatValue(chainId: Int, address: String): Flow<Double>

    /**
     * Get portfolio chart data.
     */
    suspend fun getPortfolioChartData(
        chainId: Int,
        days: Int,
    ): DataResult<List<PricePoint>>

    /**
     * Get a single token's detail with extended price data.
     */
    suspend fun getTokenDetail(
        chainId: Int,
        contractAddress: String,
        address: String,
    ): DataResult<Token>

    /**
     * Get price chart for a specific token.
     */
    suspend fun getTokenPriceChart(
        chainId: Int,
        contractAddress: String,
        days: Int,
    ): DataResult<List<PricePoint>>
}
