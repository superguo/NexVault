package com.nexvault.wallet.domain.usecase.auth

import com.nexvault.wallet.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Checks if biometric authentication is available on the device.
 */
class CheckBiometricAvailabilityUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isBiometricAvailable()
    }
}
