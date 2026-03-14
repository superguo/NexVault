package com.nexvault.wallet.core.datastore.util

import com.nexvault.wallet.core.datastore.model.AccountMetadata
import com.nexvault.wallet.core.datastore.model.WalletMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DataStoreSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun serializeWallets(wallets: List<WalletMetadata>): String {
        return json.encodeToString(wallets)
    }

    fun deserializeWallets(jsonString: String): List<WalletMetadata> {
        return try {
            if (jsonString.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeAccounts(accounts: List<AccountMetadata>): String {
        return json.encodeToString(accounts)
    }

    fun deserializeAccounts(jsonString: String): List<AccountMetadata> {
        return try {
            if (jsonString.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
