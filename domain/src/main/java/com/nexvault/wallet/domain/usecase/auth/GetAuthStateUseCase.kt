package com.nexvault.wallet.domain.usecase.auth

import com.nexvault.wallet.domain.model.auth.AuthState
import com.nexvault.wallet.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the current authentication state.
 * Used by navigation logic to determine which screen to show.
 */
class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> {
        return authRepository.getAuthState()
    }
}
