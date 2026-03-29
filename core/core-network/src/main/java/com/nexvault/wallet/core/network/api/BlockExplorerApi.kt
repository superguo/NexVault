package com.nexvault.wallet.core.network.api

import com.nexvault.wallet.core.network.dto.EtherscanBalanceResponse
import com.nexvault.wallet.core.network.dto.EtherscanTokenTransferListResponse
import com.nexvault.wallet.core.network.dto.EtherscanTransactionListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Etherscan-compatible block explorer API service.
 *
 * This interface works with Etherscan, BscScan, and PolygonScan since they all
 * share the same API format. The base URL is set per-chain via BlockExplorerApiFactory.
 *
 * Ref: doc/05-IMPLEMENTATION-PLAN-PHASE2.md Task 2.1.3
 */
interface BlockExplorerApi {

    /**
     * Fetches normal (native coin) transactions for an address.
     *
     * @param module API module (default: "account")
     * @param action API action (default: "txlist")
     * @param address Wallet address
     * @param startBlock Starting block number (0 for all)
     * @param endBlock Ending block number (99999999 for latest)
     * @param page Page number for pagination
     * @param offset Number of results per page (max 10000)
     * @param sort Sort order ("asc" or "desc")
     * @param apiKey Explorer API key
     */
    @GET("api")
    suspend fun getTransactions(
        @Query("module") module: String = "account",
        @Query("action") action: String = "txlist",
        @Query("address") address: String,
        @Query("startblock") startBlock: Long = 0,
        @Query("endblock") endBlock: Long = 99999999,
        @Query("page") page: Int = 1,
        @Query("offset") offset: Int = 20,
        @Query("sort") sort: String = "desc",
        @Query("apikey") apiKey: String,
    ): EtherscanTransactionListResponse

    /**
     * Fetches ERC-20 token transfer events for an address.
     */
    @GET("api")
    suspend fun getTokenTransfers(
        @Query("module") module: String = "account",
        @Query("action") action: String = "tokentx",
        @Query("address") address: String,
        @Query("startblock") startBlock: Long = 0,
        @Query("endblock") endBlock: Long = 99999999,
        @Query("page") page: Int = 1,
        @Query("offset") offset: Int = 20,
        @Query("sort") sort: String = "desc",
        @Query("apikey") apiKey: String,
    ): EtherscanTokenTransferListResponse

    /**
     * Fetches the native coin balance for an address.
     *
     * Useful as a lightweight alternative to Web3j ethGetBalance.
     */
    @GET("api")
    suspend fun getBalance(
        @Query("module") module: String = "account",
        @Query("action") action: String = "balance",
        @Query("address") address: String,
        @Query("tag") tag: String = "latest",
        @Query("apikey") apiKey: String,
    ): EtherscanBalanceResponse
}
