package com.nexvault.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.nexvault.wallet.core.datastore.security.SecurityPreferencesDataStore
import com.nexvault.wallet.core.security.biometric.BiometricHelper
import com.nexvault.wallet.feature.auth.navigation.AuthRoutes
import com.nexvault.wallet.feature.auth.navigation.authGraph
import com.nexvault.wallet.feature.onboarding.navigation.OnboardingRoutes
import com.nexvault.wallet.feature.onboarding.navigation.onboardingGraph
import com.nexvault.wallet.navigation.MainRoutes
import com.nexvault.wallet.navigation.mainGraph
import kotlinx.coroutines.flow.StateFlow

/**
 * Root composable for the NexVault app.
 *
 * Determines the start destination based on:
 * 1. Is a wallet created? (from SecurityPreferencesDataStore.isWalletSetUp)
 *    - NO  → onboarding_graph (create or import wallet)
 *    - YES → check authentication state
 * 2. Is the user authenticated? (from isAuthenticated StateFlow)
 *    - NO  → auth_graph (unlock screen)
 *    - YES → main_graph (home, history, dapp, nft, settings)
 *
 * Navigation flow:
 * - First launch: onboarding_graph → (after PIN set) → main_graph
 * - Returning user: auth_graph → (after unlock) → main_graph
 * - After auto-lock: auth_graph → (after unlock) → main_graph
 */
@Composable
fun NexVaultApp(
    biometricHelper: BiometricHelper,
    isAuthenticated: StateFlow<Boolean>,
    onAuthSuccess: () -> Unit,
    onAuthRequired: () -> Unit,
    securityPreferences: SecurityPreferencesDataStore,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val isAuthed by isAuthenticated.collectAsStateWithLifecycle(initialValue = false)

    // Observe wallet creation state from SecurityPreferencesDataStore
    val isWalletSetUp by securityPreferences.isWalletSetUp.collectAsStateWithLifecycle(
        initialValue = false
    )

    // Determine routing key based on state
    val routingKey = remember(isWalletSetUp, isAuthed) {
        when {
            !isWalletSetUp -> "onboarding"
            !isAuthed -> "auth"
            else -> "main"
        }
    }

    key(routingKey) {
        NavHost(
            navController = navController,
            startDestination = when (routingKey) {
                "onboarding" -> OnboardingRoutes.ONBOARDING_GRAPH
                "auth" -> AuthRoutes.AUTH_GRAPH
                else -> MainRoutes.MAIN_GRAPH
            },
            modifier = modifier,
        ) {
            // Onboarding graph
            onboardingGraph(
                navController = navController,
                onOnboardingComplete = {
                    onAuthSuccess()
                },
            )

            // Auth graph
            authGraph(
                navController = navController,
                biometricHelper = biometricHelper,
                onAuthSuccess = {
                    onAuthSuccess()
                },
                onNavigateToOnboarding = {
                    navController.navigate(OnboardingRoutes.ONBOARDING_GRAPH) {
                        popUpTo(AuthRoutes.AUTH_GRAPH) {
                            inclusive = true
                        }
                    }
                },
            )

            // Main graph
            mainGraph()
        }
    }
}
