package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from Etherscan-compatible account tokentx API.
 *
 * This endpoint returns ERC-20 token transfer events for an address.
 *
 * @property status "1" for success, "0" for error
 * @property message Status message from the API
 * @property result List of token transfer DTOs
 */
@JsonClass(generateAdapter = true)
data class EtherscanTokenTransferListResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "result") val result: List<TokenTransferDto>,
) {
    val isSuccess: Boolean get() = status == "1"
}
