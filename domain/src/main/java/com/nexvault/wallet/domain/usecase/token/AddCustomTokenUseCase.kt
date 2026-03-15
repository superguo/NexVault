package com.nexvault.wallet.domain.usecase.token

import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.model.common.InvalidAddressException
import com.nexvault.wallet.domain.model.token.Token
import com.nexvault.wallet.domain.repository.TokenRepository
import javax.inject.Inject

/**
 * Adds a custom ERC-20 token by contract address.
 * Validates the address format, then fetches token info from chain.
 */
class AddCustomTokenUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(
        chainId: Int,
        contractAddress: String,
    ): DataResult<Token> {
        val cleaned = contractAddress.trim()
        if (!isValidAddress(cleaned)) {
            return DataResult.Error(InvalidAddressException())
        }
        return tokenRepository.addCustomToken(chainId, cleaned)
    }

    private fun isValidAddress(address: String): Boolean {
        return address.startsWith("0x") &&
            address.length == 42 &&
            address.substring(2).all { it in "0123456789abcdefABCDEF" }
    }
}
