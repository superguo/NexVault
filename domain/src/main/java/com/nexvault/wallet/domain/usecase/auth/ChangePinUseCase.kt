package com.nexvault.wallet.domain.usecase.auth

import com.nexvault.wallet.domain.model.common.AuthenticationException
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Changes the user's PIN.
 * Verifies the current PIN, validates the new PIN, then stores.
 */
class ChangePinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        currentPin: String,
        newPin: String,
    ): DataResult<Unit> {
        if (newPin.length != 6 || !newPin.all { it.isDigit() }) {
            return DataResult.Error(
                AuthenticationException("New PIN must be exactly 6 digits"),
            )
        }
        if (currentPin == newPin) {
            return DataResult.Error(
                AuthenticationException("New PIN must be different from current PIN"),
            )
        }
        return authRepository.changePin(currentPin, newPin)
    }
}
