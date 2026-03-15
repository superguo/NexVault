package com.nexvault.wallet.domain.usecase.wallet

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Retrieves the mnemonic for backup display.
 * Caller must ensure authentication has been performed before calling.
 */
class GetMnemonicForBackupUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(walletId: String): DataResult<List<String>> {
        return walletRepository.getMnemonicForBackup(walletId)
    }
}
