package com.nexvault.wallet.core.network.web3

import com.nexvault.wallet.core.network.config.ChainConfigProvider
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides and caches Web3j instances per chain.
 *
 * Web3j instances are expensive to create (they set up HTTP connections).
 * This class reuses them via a ConcurrentHashMap keyed by chain ID.
 *
 * Ref: doc/09-PERFORMANCE-OPTIMIZATION.md Section 5.2 - Web3j Instance Caching
 * Ref: doc/05-IMPLEMENTATION-PLAN-PHASE2.md Task 2.1.4
 *
 * Usage:
 * ```
 * val web3j = web3jProvider.getWeb3j(chainId = 1)
 * val blockNumber = web3j.ethBlockNumber().send().blockNumber
 * ```
 */
@Singleton
class Web3jProvider @Inject constructor(
    private val chainConfigProvider: ChainConfigProvider,
) {
    private val instances = ConcurrentHashMap<Int, Web3j>()

    /**
     * Returns a cached Web3j instance for the given chain ID.
     *
     * Creates a new one if none exists.
     *
     * @param chainId The chain ID (1 for Ethereum mainnet, etc.)
     * @return Web3j instance connected to the chain's RPC URL
     * @throws IllegalArgumentException if the chain ID is not supported
     */
    fun getWeb3j(chainId: Int): Web3j {
        return instances.getOrPut(chainId) {
            val config = chainConfigProvider.getConfig(chainId)
                ?: throw IllegalArgumentException("Unsupported chain ID: $chainId")
            Web3j.build(HttpService(config.rpcUrl))
        }
    }

    /**
     * Shuts down and removes the Web3j instance for a given chain.
     *
     * Call this when a chain is deselected for a long time to free resources.
     */
    fun shutdown(chainId: Int) {
        instances.remove(chainId)?.shutdown()
    }

    /**
     * Shuts down all cached Web3j instances.
     *
     * Call this when the app is being destroyed.
     */
    fun shutdownAll() {
        instances.values.forEach { it.shutdown() }
        instances.clear()
    }
}
