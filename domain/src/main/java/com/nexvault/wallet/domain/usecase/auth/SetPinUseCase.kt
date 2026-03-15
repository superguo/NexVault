package com.nexvault.wallet.domain.usecase.auth

import com.nexvault.wallet.domain.model.common.AuthenticationException
import com.nexvault.wallet.domain.model.common.DataResult
import com.nexvault.wallet.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Sets the user's PIN after wallet creation/import.
 * Validates PIN format before storing.
 */
class SetPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(pin: String): DataResult<Unit> {
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return DataResult.Error(
                AuthenticationException("PIN must be exactly 6 digits"),
            )
        }
        return authRepository.setPin(pin)
    }
}
