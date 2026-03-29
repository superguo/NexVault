package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from Etherscan-compatible account balance API.
 *
 * This is a lightweight alternative to Web3j ethGetBalance for
 * checking native coin balances.
 *
 * @property status "1" for success, "0" for error
 * @property message Status message from the API
 * @property result Balance in wei as a string
 */
@JsonClass(generateAdapter = true)
data class EtherscanBalanceResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "result") val result: String,
) {
    val isSuccess: Boolean get() = status == "1"
}
