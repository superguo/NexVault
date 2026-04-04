package com.nexvault.wallet.core.ui.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a fiat amount as USD with grouping and two decimals.
 *
 * @param value Amount in USD.
 */
fun formatFiatValue(value: Double): String {
    return "$${String.format("%,.2f", value)}"
}

/**
 * Formats a token balance for display, trimming excess decimals.
 *
 * @param balance Raw balance.
 */
fun formatTokenBalance(balance: BigDecimal): String {
    return if (balance.scale() > 6) {
        balance.setScale(6, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    } else {
        balance.stripTrailingZeros().toPlainString()
    }
}

/**
 * Formats a human-readable token amount for transaction rows.
 *
 * @param value Amount in token units (already scaled from chain units).
 * @param maxFractionDigits Maximum fraction digits to keep (default 6).
 */
fun formatTransactionAmount(value: BigDecimal, maxFractionDigits: Int = 6): String {
    val scaled = if (value.scale() > maxFractionDigits) {
        value.setScale(maxFractionDigits, RoundingMode.DOWN)
    } else {
        value
    }
    return scaled.stripTrailingZeros().toPlainString()
}

/**
 * Truncates an Ethereum-style address for display.
 *
 * @param address Full hex address or short string.
 */
fun truncateAddress(address: String): String {
    if (address.length < 10) return address
    return "${address.take(6)}...${address.takeLast(4)}"
}

/**
 * Formats a Unix timestamp in seconds to a short date string.
 *
 * @param timestampSeconds Seconds since epoch.
 */
fun formatTimestamp(timestampSeconds: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestampSeconds * 1000))
}
