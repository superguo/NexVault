package com.nexvault.wallet.domain.model.wallet

/**
 * Represents a wallet in the domain layer.
 * A wallet contains one or more accounts derived from a single mnemonic (HD)
 * or a single imported private key.
 */
data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType,
    val accounts: List<Account>,
    val createdAt: Long,
    val isActive: Boolean,
)

enum class WalletType {
    HD,
    IMPORTED,
}

data class Account(
    val walletId: String,
    val index: Int,
    val address: String,
    val name: String,
    val derivationPath: String,
    val isActive: Boolean,
)
