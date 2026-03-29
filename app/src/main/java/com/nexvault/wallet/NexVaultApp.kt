package com.nexvault.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.nexvault.wallet.core.datastore.security.SecurityPreferencesDataStore
import com.nexvault.wallet.core.security.biometric.BiometricHelper
import com.nexvault.wallet.feature.auth.navigation.AuthRoutes
import com.nexvault.wallet.feature.auth.navigation.authGraph
import com.nexvault.wallet.feature.onboarding.navigation.OnboardingRoutes
import com.nexvault.wallet.feature.onboarding.navigation.onboardingGraph
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
                else -> "main_graph"
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

            // Main graph — PLACEHOLDER for now (Prompt 10 will build this)
            navigation(
                route = "main_graph",
                startDestination = "home_placeholder",
            ) {
                composable("home_placeholder") {
                    MainGraphPlaceholder(
                        onNavigateToAuth = {
                            onAuthRequired()
                            navController.navigate(AuthRoutes.AUTH_GRAPH) {
                                popUpTo("main_graph") {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MainGraphPlaceholder(
    onNavigateToAuth: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Welcome to NexVault!",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Main screen coming in Prompt 10",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Currently authenticated",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
