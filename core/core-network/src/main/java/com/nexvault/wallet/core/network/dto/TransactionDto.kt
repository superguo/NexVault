package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Transaction DTO from Etherscan txlist API.
 *
 * All numeric fields are strings because JSON numbers may lose precision
 * for large values like wei amounts.
 *
 * @property hash Transaction hash
 * @property from From address
 * @property to To address
 * @property value Value in wei
 * @property gas Gas limit
 * @property gasPrice Gas price in wei
 * @property gasUsed Actual gas used
 * @property blockNumber Block number
 * @property timestamp Unix timestamp
 * @property nonce Nonce
 * @property isError "0" for success, "1" for error
 * @property txReceiptStatus "1" for success, "0" for failure
 * @property input Input data
 * @property contractAddress Contract address (for contract creation)
 * @property confirmations Number of confirmations
 */
@JsonClass(generateAdapter = true)
data class TransactionDto(
    @Json(name = "hash") val hash: String,
    @Json(name = "from") val from: String,
    @Json(name = "to") val to: String,
    @Json(name = "value") val value: String,
    @Json(name = "gas") val gas: String,
    @Json(name = "gasPrice") val gasPrice: String,
    @Json(name = "gasUsed") val gasUsed: String?,
    @Json(name = "blockNumber") val blockNumber: String,
    @Json(name = "timeStamp") val timestamp: String,
    @Json(name = "nonce") val nonce: String,
    @Json(name = "isError") val isError: String,
    @Json(name = "txreceipt_status") val txReceiptStatus: String?,
    @Json(name = "input") val input: String,
    @Json(name = "contractAddress") val contractAddress: String?,
    @Json(name = "confirmations") val confirmations: String,
)
