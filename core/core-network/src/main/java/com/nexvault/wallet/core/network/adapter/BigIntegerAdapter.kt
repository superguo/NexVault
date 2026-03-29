package com.nexvault.wallet.core.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.math.BigInteger

/**
 * Moshi adapter for BigInteger values.
 *
 * Handles both decimal strings and hexadecimal strings (0x prefix) commonly
 * used in Ethereum APIs. Output is always a decimal string.
 *
 * Used for gas prices, nonces, balances, and other large integer values
 * from blockchain RPCs and explorer APIs.
 */
class BigIntegerAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BigInteger {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> {
                val longValue = reader.nextLong()
                BigInteger.valueOf(longValue)
            }
            JsonReader.Token.STRING -> {
                val stringValue = reader.nextString()
                parseBigInteger(stringValue)
            }
            else -> throw JsonDataException("Expected number or string for BigInteger")
        }
    }

    @ToJson
    fun toJson(value: BigInteger): String {
        return value.toString()
    }

    private fun parseBigInteger(value: String): BigInteger {
        return try {
            if (value.startsWith("0x") || value.startsWith("0X")) {
                BigInteger(value.removePrefix("0x").removePrefix("0X"), 16)
            } else {
                BigInteger(value)
            }
        } catch (e: NumberFormatException) {
            throw JsonDataException("Invalid BigInteger: $value")
        }
    }
}
