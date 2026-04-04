package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.token.Token
import com.nexvault.wallet.domain.repository.TokenRepository
import com.nexvault.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Observes a single token from Room for the active wallet context.
 *
 * When no wallet is active, emits a single null value.
 *
 * @param contractAddress Token contract address or [com.nexvault.wallet.domain.model.token.Token.NATIVE_TOKEN_ADDRESS].
 * @param chainId Chain ID.
 */
class ObserveTokenDetailUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val walletRepository: WalletRepository,
) {
    operator fun invoke(contractAddress: String, chainId: Int): Flow<Token?> {
        return walletRepository.getActiveWallet().flatMapLatest { wallet ->
            if (wallet != null) {
                tokenRepository.observeToken(chainId, contractAddress)
            } else {
                flowOf(null)
            }
        }
    }
}
