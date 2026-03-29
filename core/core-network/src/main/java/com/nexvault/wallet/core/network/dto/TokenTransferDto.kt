package com.nexvault.wallet.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * ERC-20 token transfer DTO from Etherscan tokentx API.
 *
 * Represents a token transfer event including token details and gas used.
 *
 * @property hash Transaction hash
 * @property from From address
 * @property to To address
 * @property value Transfer value (token amount, not wei)
 * @property tokenName Name of the token
 * @property tokenSymbol Symbol of the token
 * @property tokenDecimal Token decimals
 * @property contractAddress Token contract address
 * @property gas Gas limit
 * @property gasPrice Gas price in wei
 * @property gasUsed Actual gas used
 * @property blockNumber Block number
 * @property timestamp Unix timestamp
 * @property nonce Nonce
 * @property confirmations Number of confirmations
 */
@JsonClass(generateAdapter = true)
data class TokenTransferDto(
    @Json(name = "hash") val hash: String,
    @Json(name = "from") val from: String,
    @Json(name = "to") val to: String,
    @Json(name = "value") val value: String,
    @Json(name = "tokenName") val tokenName: String,
    @Json(name = "tokenSymbol") val tokenSymbol: String,
    @Json(name = "tokenDecimal") val tokenDecimal: String,
    @Json(name = "contractAddress") val contractAddress: String,
    @Json(name = "gas") val gas: String?,
    @Json(name = "gasPrice") val gasPrice: String?,
    @Json(name = "gasUsed") val gasUsed: String?,
    @Json(name = "blockNumber") val blockNumber: String,
    @Json(name = "timeStamp") val timestamp: String,
    @Json(name = "nonce") val nonce: String?,
    @Json(name = "confirmations") val confirmations: String?,
)
