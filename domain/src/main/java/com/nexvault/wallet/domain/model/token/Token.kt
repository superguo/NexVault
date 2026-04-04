package com.nexvault.wallet.domain.model.token

import java.math.BigDecimal

/**
 * Domain model for a token (native coin or ERC-20).
 *
 * @property contractAddress `"native"` for native coins, or ERC-20 contract address (lowercase).
 * @property coinGeckoId CoinGecko coin id for price APIs; null for some custom tokens.
 * @property isCustom Whether the user added this token manually.
 */
data class Token(
    val contractAddress: String,
    val chainId: Int,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val logoUrl: String?,
    val balance: BigDecimal,
    val fiatPrice: Double?,
    val fiatValue: Double?,
    val priceChange24h: Double?,
    val coinGeckoId: String? = null,
    val isCustom: Boolean = false,
) {
    val isNative: Boolean get() = contractAddress == NATIVE_TOKEN_ADDRESS

    companion object {
        const val NATIVE_TOKEN_ADDRESS = "native"
    }
}

/**
 * Portfolio aggregation of all tokens.
 */
data class Portfolio(
    val totalFiatValue: Double,
    val change24hPercent: Double,
    val tokens: List<Token>,
    val chartData: List<PricePoint>,
)

data class PricePoint(
    val timestamp: Long,
    val value: Double,
)
