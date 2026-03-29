package com.nexvault.wallet.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.nexvault.wallet.core.security.biometric.BiometricHelper
import com.nexvault.wallet.feature.auth.screen.UnlockScreen

/**
 * Route constants for the auth graph.
 */
object AuthRoutes {
    const val AUTH_GRAPH = "auth_graph"
    const val UNLOCK = "unlock"
}

/**
 * Registers the auth navigation graph.
 *
 * @param navController the NavController for navigation
 * @param biometricHelper the BiometricHelper instance (from Activity or Hilt)
 * @param onAuthSuccess callback when authentication succeeds.
 *        The caller should navigate to the main graph and clear the auth backstack.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    biometricHelper: BiometricHelper,
    onAuthSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    navigation(
        route = AuthRoutes.AUTH_GRAPH,
        startDestination = AuthRoutes.UNLOCK,
    ) {
        composable(route = AuthRoutes.UNLOCK) {
            UnlockScreen(
                onNavigateToMain = onAuthSuccess,
                onNavigateToOnboarding = onNavigateToOnboarding,
                biometricHelper = biometricHelper,
            )
        }
    }
}
