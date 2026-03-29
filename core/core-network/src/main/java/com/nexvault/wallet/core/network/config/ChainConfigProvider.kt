package com.nexvault.wallet.core.network.config

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides network configurations for all supported blockchain chains.
 *
 * API keys are injected via Hilt from the app module's BuildConfig.
 * Each chain has its own RPC URL, explorer API endpoint, and CoinGecko ID.
 *
 * Supported chains:
 * - Ethereum Mainnet (chainId=1)
 * - Ethereum Sepolia testnet (chainId=11155111)
 * - BNB Smart Chain (chainId=56)
 * - Polygon (chainId=137)
 *
 * @param infuraApiKey Infura API key for Ethereum RPC
 * @param alchemyApiKey Alchemy API key for Ethereum RPC (alternative to Infura)
 * @param etherscanApiKey Etherscan API key for block explorer API
 */
@Singleton
class ChainConfigProvider @Inject constructor(
    private val infuraApiKey: String,
    private val alchemyApiKey: String,
    private val etherscanApiKey: String,
) {
    /**
     * Returns the network config for a given chain ID.
     *
     * @param chainId The chain ID to look up
     * @return The chain configuration, or null if the chain is not supported
     */
    fun getConfig(chainId: Int): ChainNetworkConfig? = configs[chainId]

    /**
     * Returns all supported chain configurations.
     */
    fun getAllConfigs(): List<ChainNetworkConfig> = configs.values.toList()

    private val configs: Map<Int, ChainNetworkConfig> = mapOf(
        1 to ChainNetworkConfig(
            chainId = 1,
            rpcUrl = "https://mainnet.infura.io/v3/$infuraApiKey",
            explorerApiBaseUrl = "https://api.etherscan.io/",
            explorerApiKey = etherscanApiKey,
            coinGeckoNativeCoinId = "ethereum",
            isTestnet = false,
        ),
        11155111 to ChainNetworkConfig(
            chainId = 11155111,
            rpcUrl = "https://sepolia.infura.io/v3/$infuraApiKey",
            explorerApiBaseUrl = "https://api-sepolia.etherscan.io/",
            explorerApiKey = etherscanApiKey,
            coinGeckoNativeCoinId = "ethereum",
            isTestnet = true,
        ),
        56 to ChainNetworkConfig(
            chainId = 56,
            rpcUrl = "https://bsc-dataseed.binance.org/",
            explorerApiBaseUrl = "https://api.bscscan.com/",
            explorerApiKey = "",
            coinGeckoNativeCoinId = "binancecoin",
            isTestnet = false,
        ),
        137 to ChainNetworkConfig(
            chainId = 137,
            rpcUrl = "https://polygon-rpc.com/",
            explorerApiBaseUrl = "https://api.polygonscan.com/",
            explorerApiKey = "",
            coinGeckoNativeCoinId = "matic-network",
            isTestnet = false,
        ),
    )
}
