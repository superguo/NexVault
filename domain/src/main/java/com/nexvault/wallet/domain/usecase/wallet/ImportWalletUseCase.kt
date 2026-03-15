package com.nexvault.wallet.domain.usecase.wallet

import com.nexvault.wallet.domain.model.auth.WalletCreationResult
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.common.InvalidMnemonicException
import com.nexvault.wallet.domain.model.common.InvalidPrivateKeyException
import com.nexvault.wallet.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Imports a wallet from a mnemonic phrase or private key.
 */
class ImportWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    /**
     * Import from BIP-39 mnemonic (12 or 24 words).
     */
    suspend fun fromMnemonic(
        mnemonic: String,
        walletName: String = "Imported Wallet",
    ): DataResult<WalletCreationResult> {
        val trimmed = mnemonic.trim().lowercase()
        val words = trimmed.split("\\s+".toRegex())
        if (words.size != 12 && words.size != 24) {
            return DataResult.Error(
                InvalidMnemonicException("Mnemonic must be 12 or 24 words, got ${words.size}"),
            )
        }
        return walletRepository.importFromMnemonic(trimmed, walletName)
    }

    /**
     * Import from raw private key (64 hex chars).
     */
    suspend fun fromPrivateKey(
        privateKey: String,
        walletName: String = "Imported Wallet",
    ): DataResult<WalletCreationResult> {
        val cleaned = privateKey.trim().removePrefix("0x").removePrefix("0X")
        if (cleaned.length != 64 || !cleaned.all { it in "0123456789abcdefABCDEF" }) {
            return DataResult.Error(
                InvalidPrivateKeyException("Private key must be 64 hex characters"),
            )
        }
        return walletRepository.importFromPrivateKey(cleaned, walletName)
    }
}
