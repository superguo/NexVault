package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from CoinGecko /simple/price endpoint.
 *
 * Example response:
 * {
 *   "ethereum": {
 *     "usd": 1650.42,
 *     "usd_24h_change": 2.34
 *   }
 * }
 *
 * The outer map key is the CoinGecko coin ID (e.g., "ethereum").
 * The value is TokenPriceResponse with dynamic currency fields.
 *
 * @property usd Price in USD
 * @property usd24hChange 24-hour price change percentage
 */
@JsonClass(generateAdapter = true)
data class TokenPriceResponse(
    @Json(name = "usd") val usd: Double?,
    @Json(name = "usd_24h_change") val usd24hChange: Double?,
)
