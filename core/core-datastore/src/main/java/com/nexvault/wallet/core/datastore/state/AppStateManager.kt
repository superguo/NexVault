package com.nexvault.wallet.core.datastore.state

import com.nexvault.wallet.core.datastore.model.AutoLockTimeout
import com.nexvault.wallet.core.datastore.model.NetworkType
import com.nexvault.wallet.core.datastore.preferences.UserPreferencesDataStore
import com.nexvault.wallet.core.datastore.security.SecurityPreferencesDataStore
import com.nexvault.wallet.core.datastore.wallet.WalletMetadataDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class WalletDisplayInfo(
    val walletName: String,
    val accountName: String,
    val address: String,
    val network: NetworkType
)

sealed class AuthFailureResult {
    data class TemporaryLockout(val seconds: Long, val attemptCount: Int) : AuthFailureResult()
    data class Warning(val message: String, val attemptCount: Int) : AuthFailureResult()
    object WalletWiped : AuthFailureResult()
    data class NoLockout(val attemptCount: Int) : AuthFailureResult()
}

@Singleton
class AppStateManager @Inject constructor(
    private val userPreferences: UserPreferencesDataStore,
    private val securityPreferences: SecurityPreferencesDataStore,
    private val walletMetadata: WalletMetadataDataStore
) {
    companion object {
        private const val LOCKOUT_5_ATTEMPTS = 30L
        private const val LOCKOUT_8_ATTEMPTS = 300L
        private const val LOCKOUT_10_ATTEMPTS = 900L
        private const val LOCKOUT_15_ATTEMPTS = 3600L
        private const val LOCKOUT_20_ATTEMPTS = -1
    }

    val isFirstRun: Flow<Boolean> = combine(
        securityPreferences.isWalletSetUp,
        userPreferences.hasCompletedOnboarding
    ) { isWalletSetUp, hasCompletedOnboarding ->
        !isWalletSetUp && !hasCompletedOnboarding
    }

    val isWalletLocked: Flow<Boolean> = combine(
        securityPreferences.isWalletSetUp,
        securityPreferences.lastAuthTimestamp,
        userPreferences.autoLockTimeout
    ) { isSetUp, lastAuth, timeout ->
        if (!isSetUp) return@combine false

        if (timeout == AutoLockTimeout.NEVER) {
            return@combine false
        }

        if (timeout == AutoLockTimeout.IMMEDIATE) {
            return@combine true
        }

        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = (currentTime - lastAuth) / 1000
        elapsedSeconds > timeout.seconds
    }

    val isLockedOut: Flow<Boolean> = combine(
        securityPreferences.failedAttemptCount,
        securityPreferences.lockoutEndTime
    ) { attemptCount, lockoutEndTime ->
        if (attemptCount < 5) return@combine false

        val currentTime = System.currentTimeMillis()
        if (lockoutEndTime > currentTime) {
            return@combine true
        }
        false
    }

    val lockoutRemainingSeconds: Flow<Long> = securityPreferences.lockoutEndTime.map { lockoutEndTime ->
        val currentTime = System.currentTimeMillis()
        if (lockoutEndTime > currentTime) {
            (lockoutEndTime - currentTime) / 1000
        } else {
            0L
        }
    }

    val currentWalletDisplayInfo: Flow<WalletDisplayInfo?> = combine(
        walletMetadata.wallets,
        userPreferences.selectedNetwork
    ) { wallets, network ->
        val activeWallet = wallets.find { it.isActive } ?: return@combine null

        WalletDisplayInfo(
            walletName = activeWallet.name,
            accountName = "Account 1",
            address = "",
            network = network
        )
    }

    suspend fun onAuthenticationSuccess() {
        securityPreferences.resetFailedAttempts()
        securityPreferences.setLockoutEndTime(0L)
        securityPreferences.setLastAuthTimestamp(System.currentTimeMillis())
    }

    suspend fun onAuthenticationFailure(): AuthFailureResult {
        val newCount = securityPreferences.incrementFailedAttempts()
        val currentTime = System.currentTimeMillis()

        val lockoutSeconds = when {
            newCount >= 20 -> LOCKOUT_20_ATTEMPTS
            newCount >= 15 -> LOCKOUT_15_ATTEMPTS
            newCount >= 10 -> LOCKOUT_10_ATTEMPTS
            newCount >= 8 -> LOCKOUT_8_ATTEMPTS
            newCount >= 5 -> LOCKOUT_5_ATTEMPTS
            else -> 0L
        }

        return when {
            newCount >= 20 -> {
                securityPreferences.setLockoutEndTime(0L)
                AuthFailureResult.WalletWiped
            }
            newCount >= 15 -> {
                val lockoutEnd = currentTime + (LOCKOUT_15_ATTEMPTS * 1000)
                securityPreferences.setLockoutEndTime(lockoutEnd)
                AuthFailureResult.Warning(
                    message = "Too many failed attempts. You have 1 hour before wallet wipe warning.",
                    attemptCount = newCount
                )
            }
            newCount >= 10 -> {
                val lockoutEnd = currentTime + (LOCKOUT_10_ATTEMPTS * 1000)
                securityPreferences.setLockoutEndTime(lockoutEnd)
                AuthFailureResult.TemporaryLockout(
                    seconds = LOCKOUT_10_ATTEMPTS,
                    attemptCount = newCount
                )
            }
            newCount >= 8 -> {
                val lockoutEnd = currentTime + (LOCKOUT_8_ATTEMPTS * 1000)
                securityPreferences.setLockoutEndTime(lockoutEnd)
                AuthFailureResult.TemporaryLockout(
                    seconds = LOCKOUT_8_ATTEMPTS,
                    attemptCount = newCount
                )
            }
            newCount >= 5 -> {
                val lockoutEnd = currentTime + (LOCKOUT_5_ATTEMPTS * 1000)
                securityPreferences.setLockoutEndTime(lockoutEnd)
                AuthFailureResult.TemporaryLockout(
                    seconds = LOCKOUT_5_ATTEMPTS,
                    attemptCount = newCount
                )
            }
            else -> {
                AuthFailureResult.NoLockout(newCount)
            }
        }
    }

    suspend fun isAuthenticationRequired(): Boolean {
        val isSetUp = securityPreferences.isWalletSetUp.first()
        if (!isSetUp) return false

        val autoLockTimeout = userPreferences.autoLockTimeout.first()
        val lastAuth = securityPreferences.lastAuthTimestamp.first()
        val currentTime = System.currentTimeMillis()

        return when (autoLockTimeout) {
            AutoLockTimeout.NEVER -> false
            AutoLockTimeout.IMMEDIATE -> true
            else -> {
                val elapsedSeconds = (currentTime - lastAuth) / 1000
                elapsedSeconds > autoLockTimeout.seconds
            }
        }
    }

    suspend fun resetApp() {
        userPreferences.clearAll()
        securityPreferences.clearAll()
        walletMetadata.clearAll()
    }
}
