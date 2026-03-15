package com.nexvault.wallet.domain.repository

import com.nexvault.wallet.domain.model.auth.AuthResult
import com.nexvault.wallet.domain.model.auth.AuthState
import com.nexvault.wallet.domain.model.common.DataResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for authentication operations.
 */
interface AuthRepository {
    /**
     * Set the user's PIN (6 digits).
     * Stores a salted hash, never the plaintext PIN.
     */
    suspend fun setPin(pin: String): DataResult<Unit>

    /**
     * Verify the entered PIN against the stored hash.
     * Handles progressive lockout via AppStateManager.
     */
    suspend fun verifyPin(pin: String): AuthResult

    /**
     * Change the PIN (requires verification of old PIN first).
     */
    suspend fun changePin(currentPin: String, newPin: String): DataResult<Unit>

    /**
     * Check if biometric authentication is available on this device.
     */
    suspend fun isBiometricAvailable(): Boolean

    /**
     * Enable or disable biometric authentication.
     */
    suspend fun setBiometricEnabled(enabled: Boolean): DataResult<Unit>

    /**
     * Get the current biometric enabled state.
     */
    fun isBiometricEnabled(): Flow<Boolean>

    /**
     * Get the current authentication state.
     */
    fun getAuthState(): Flow<AuthState>

    /**
     * Record a successful authentication (resets lockout, updates timestamp).
     */
    suspend fun onAuthSuccess()

    /**
     * Check if authentication is required (based on auto-lock timeout).
     */
    suspend fun isAuthRequired(): Boolean

    /**
     * Check if a PIN has been set.
     */
    fun hasPinSet(): Flow<Boolean>

    /**
     * Get the configured auto-lock timeout in seconds.
     */
    fun getAutoLockTimeout(): Flow<Long>

    /**
     * Set the auto-lock timeout.
     */
    suspend fun setAutoLockTimeout(timeoutSeconds: Long): DataResult<Unit>
}
