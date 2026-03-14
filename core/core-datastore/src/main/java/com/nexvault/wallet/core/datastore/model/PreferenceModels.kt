package com.nexvault.wallet.core.datastore.model

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class NetworkType {
    MAINNET,
    GOERLI,
    SEPOLIA,
    CUSTOM
}

enum class AutoLockTimeout(val seconds: Long) {
    IMMEDIATE(0),
    ONE_MINUTE(60),
    FIVE_MINUTES(300),
    FIFTEEN_MINUTES(900),
    THIRTY_MINUTES(1800),
    NEVER(-1);

    companion object {
        fun fromSeconds(seconds: Long): AutoLockTimeout {
            return entries.find { it.seconds == seconds } ?: FIVE_MINUTES
        }
    }
}
