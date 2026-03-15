package com.nexvault.wallet.domain.usecase.wallet

import com.nexvault.wallet.domain.model.wallet.Wallet
import com.nexvault.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the currently active wallet.
 */
class GetActiveWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    operator fun invoke(): Flow<Wallet?> {
        return walletRepository.getActiveWallet()
    }
}
