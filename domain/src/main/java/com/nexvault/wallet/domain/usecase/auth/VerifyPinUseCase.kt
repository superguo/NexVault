package com.nexvault.wallet.domain.usecase.auth

import com.nexvault.wallet.domain.model.auth.AuthResult
import com.nexvault.wallet.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Verifies the user's PIN for unlock or transaction confirmation.
 * Handles progressive lockout tracking.
 */
class VerifyPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(pin: String): AuthResult {
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return AuthResult.Failed(
                remainingAttempts = null,
                message = "Invalid PIN format",
            )
        }
        return authRepository.verifyPin(pin)
    }
}
