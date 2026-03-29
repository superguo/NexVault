package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from CoinGecko /coins/{id}/market_chart endpoint.
 *
 * Example response:
 * {
 *   "prices": [[1709686800000, 1650.42], [1709690400000, 1652.10], ...]
 * }
 *
 * Each element in "prices" is a two-element array: [timestamp_ms, price].
 *
 * @property prices List of [timestamp_ms, price] pairs
 */
@JsonClass(generateAdapter = true)
data class PriceHistoryResponse(
    @Json(name = "prices") val prices: List<List<Double>>,
)
