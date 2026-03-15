package com.nexvault.wallet.domain.repository

import com.nexvault.wallet.domain.model.auth.WalletCreationResult
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.wallet.Account
import com.nexvault.wallet.domain.model.wallet.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository for wallet lifecycle operations.
 * Implementation lives in the `data` module.
 */
interface WalletRepository {
    /**
     * Create a new HD wallet with a fresh mnemonic.
     * Returns the mnemonic words (for user backup) and derived address.
     * The mnemonic is encrypted and stored securely.
     */
    suspend fun createWallet(walletName: String): DataResult<WalletCreationResult>

    /**
     * Import a wallet from a BIP-39 mnemonic phrase (12 or 24 words).
     */
    suspend fun importFromMnemonic(
        mnemonic: String,
        walletName: String,
    ): DataResult<WalletCreationResult>

    /**
     * Import a wallet from a raw private key (64 hex chars, with or without 0x prefix).
     */
    suspend fun importFromPrivateKey(
        privateKey: String,
        walletName: String,
    ): DataResult<WalletCreationResult>

    /**
     * Get all wallets with their accounts.
     */
    fun getWallets(): Flow<List<Wallet>>

    /**
     * Get the currently active wallet with its accounts.
     */
    fun getActiveWallet(): Flow<Wallet?>

    /**
     * Set a wallet as the active wallet.
     */
    suspend fun setActiveWallet(walletId: String): DataResult<Unit>

    /**
     * Add a new derived account to an existing HD wallet.
     * Returns the new account with its address.
     */
    suspend fun addAccount(walletId: String, accountName: String): DataResult<Account>

    /**
     * Get the active account's Ethereum address.
     */
    fun getActiveAddress(): Flow<String?>

    /**
     * Retrieve the mnemonic for backup display (requires prior authentication).
     * Returns the decrypted mnemonic words.
     */
    suspend fun getMnemonicForBackup(walletId: String): DataResult<List<String>>

    /**
     * Delete a wallet and all associated data.
     */
    suspend fun deleteWallet(walletId: String): DataResult<Unit>

    /**
     * Delete all wallets and reset to clean state.
     */
    suspend fun deleteAllWallets(): DataResult<Unit>

    /**
     * Check if any wallet exists.
     */
    fun hasWallet(): Flow<Boolean>
}
