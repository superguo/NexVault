package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from Etherscan-compatible account txlist API.
 *
 * Etherscan APIs return:
 * {
 *   "status": "1",      // "1" = success, "0" = error
 *   "message": "OK",
 *   "result": [ ... ]   // list of transactions
 * }
 *
 * Note: We use concrete types instead of generics to avoid Moshi codegen issues.
 *
 * @property status "1" for success, "0" for error
 * @property message Status message from the API
 * @property result List of transaction DTOs
 */
@JsonClass(generateAdapter = true)
data class EtherscanTransactionListResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "result") val result: List<TransactionDto>,
) {
    val isSuccess: Boolean get() = status == "1"
}
