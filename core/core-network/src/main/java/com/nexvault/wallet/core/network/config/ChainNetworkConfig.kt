package com.nexvault.wallet.core.network.config

/**
 * Network configuration for a specific blockchain chain.
 *
 * This data class holds all network-related configuration for a chain,
 * including RPC endpoints, explorer API URLs, and CoinGecko coin IDs.
 *
 * @property chainId The chain ID (e.g., 1 for Ethereum mainnet)
 * @property rpcUrl The RPC URL for blockchain communication
 * @property explorerApiBaseUrl The base URL for the block explorer's API
 * @property explorerApiKey The API key for the block explorer (may be empty for some explorers)
 * @property coinGeckoNativeCoinId The CoinGecko coin ID for the chain's native token
 * @property isTestnet Whether this chain is a testnet
 */
data class ChainNetworkConfig(
    val chainId: Int,
    val rpcUrl: String,
    val explorerApiBaseUrl: String,
    val explorerApiKey: String,
    val coinGeckoNativeCoinId: String,
    val isTestnet: Boolean,
)
