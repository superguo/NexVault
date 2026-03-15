package com.nexvault.wallet.domain.model.chain

/**
 * Represents a blockchain network.
 */
data class Chain(
    val chainId: Int,
    val name: String,
    val symbol: String,
    val rpcUrl: String,
    val explorerUrl: String,
    val isTestnet: Boolean,
    val iconResName: String?,
)

/**
 * Predefined supported chains.
 */
object SupportedChains {
    val ETHEREUM_MAINNET = Chain(
        chainId = 1,
        name = "Ethereum",
        symbol = "ETH",
        rpcUrl = "",
        explorerUrl = "https://etherscan.io",
        isTestnet = false,
        iconResName = "ic_ethereum",
    )

    val ETHEREUM_SEPOLIA = Chain(
        chainId = 11155111,
        name = "Sepolia Testnet",
        symbol = "ETH",
        rpcUrl = "",
        explorerUrl = "https://sepolia.etherscan.io",
        isTestnet = true,
        iconResName = "ic_ethereum",
    )

    val BSC_MAINNET = Chain(
        chainId = 56,
        name = "BNB Smart Chain",
        symbol = "BNB",
        rpcUrl = "https://bsc-dataseed.binance.org",
        explorerUrl = "https://bscscan.com",
        isTestnet = false,
        iconResName = "ic_bnb",
    )

    val POLYGON_MAINNET = Chain(
        chainId = 137,
        name = "Polygon",
        symbol = "MATIC",
        rpcUrl = "https://polygon-rpc.com",
        explorerUrl = "https://polygonscan.com",
        isTestnet = false,
        iconResName = "ic_polygon",
    )

    fun all(): List<Chain> = listOf(
        ETHEREUM_MAINNET,
        ETHEREUM_SEPOLIA,
        BSC_MAINNET,
        POLYGON_MAINNET,
    )

    fun getByChainId(chainId: Int): Chain? = all().find { it.chainId == chainId }
}
