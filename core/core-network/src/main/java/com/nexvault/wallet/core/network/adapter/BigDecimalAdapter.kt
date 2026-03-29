package com.nexvault.wallet.core.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.math.BigDecimal

/**
 * Moshi adapter for BigDecimal values.
 *
 * Handles both JSON numbers and JSON strings as input to prevent precision loss.
 * Output is always a plain string to maintain precision.
 *
 * Ref: doc/07-TEST-CASES.md TC-NET-001 - must parse "123456789.123456789"
 * without precision loss.
 */
class BigDecimalAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BigDecimal {
        return when (reader.peek()) {
            JsonReader.Token.NUMBER -> {
                val doubleValue = reader.nextDouble()
                BigDecimal(doubleValue.toString())
            }
            JsonReader.Token.STRING -> {
                val stringValue = reader.nextString()
                try {
                    BigDecimal(stringValue)
                } catch (e: NumberFormatException) {
                    throw JsonDataException("Invalid BigDecimal: $stringValue")
                }
            }
            else -> throw JsonDataException("Expected number or string for BigDecimal")
        }
    }

    @ToJson
    fun toJson(value: BigDecimal): String {
        return value.toPlainString()
    }
}
