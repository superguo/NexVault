package com.nexvault.wallet.domain.usecase.wallet

import com.nexvault.wallet.domain.model.auth.WalletCreationResult
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Creates a new HD wallet with a fresh BIP-39 mnemonic.
 *
 * Flow: generate mnemonic → derive first account → encrypt & store →
 *       update metadata → return mnemonic words for user backup.
 */
class CreateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(
        walletName: String = "Main Wallet",
    ): DataResult<WalletCreationResult> {
        return walletRepository.createWallet(walletName)
    }
}
