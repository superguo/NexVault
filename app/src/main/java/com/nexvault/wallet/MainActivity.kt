package com.nexvault.wallet

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nexvault.wallet.core.datastore.model.AutoLockTimeout
import com.nexvault.wallet.core.datastore.preferences.UserPreferencesDataStore
import com.nexvault.wallet.core.datastore.security.SecurityPreferencesDataStore
import com.nexvault.wallet.core.security.biometric.BiometricHelper
import com.nexvault.wallet.core.ui.theme.NexVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var biometricHelper: BiometricHelper

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    @Inject
    lateinit var securityPreferences: SecurityPreferencesDataStore

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private var backgroundedAt: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NexVaultTheme(darkTheme = true) {
                NexVaultApp(
                    biometricHelper = biometricHelper,
                    isAuthenticated = isAuthenticated,
                    onAuthSuccess = { _isAuthenticated.value = true },
                    onAuthRequired = { _isAuthenticated.value = false },
                    securityPreferences = securityPreferences,
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (_isAuthenticated.value) {
            backgroundedAt = System.currentTimeMillis()
        }
    }

    override fun onStart() {
        super.onStart()
        if (backgroundedAt > 0L && _isAuthenticated.value) {
            val elapsedMs = System.currentTimeMillis() - backgroundedAt

            lifecycleScope.launch {
                val autoLockTimeout = userPreferences.autoLockTimeout.first()

                val timeoutMillis = when (autoLockTimeout) {
                    AutoLockTimeout.NEVER -> Long.MAX_VALUE
                    AutoLockTimeout.IMMEDIATE -> 0L
                    else -> autoLockTimeout.seconds * 1000L
                }

                if (elapsedMs > timeoutMillis) {
                    _isAuthenticated.value = false
                }
                backgroundedAt = 0L
            }
        } else {
            backgroundedAt = 0L
        }
    }
}
