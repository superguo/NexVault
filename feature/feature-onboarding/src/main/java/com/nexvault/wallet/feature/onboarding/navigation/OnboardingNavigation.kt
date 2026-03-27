package com.nexvault.wallet.feature.onboarding.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.nexvault.wallet.feature.onboarding.screen.CreateWalletScreen
import com.nexvault.wallet.feature.onboarding.screen.VerifyMnemonicScreen
import com.nexvault.wallet.feature.onboarding.screen.WelcomeScreen

/**
 * Registers the onboarding navigation graph.
 *
 * @param navController the NavController for navigation
 * @param onOnboardingComplete callback when the full onboarding flow finishes
 *        (after PIN is set), navigates to the main graph
 */
fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onOnboardingComplete: () -> Unit,
) {
    navigation(
        route = OnboardingRoutes.ONBOARDING_GRAPH,
        startDestination = OnboardingRoutes.WELCOME,
    ) {
        composable(route = OnboardingRoutes.WELCOME) {
            WelcomeScreen(
                onNavigateToCreateWallet = {
                    navController.navigate(OnboardingRoutes.CREATE_WALLET)
                },
                onNavigateToImportWallet = {
                    navController.navigate(OnboardingRoutes.IMPORT_WALLET)
                },
            )
        }

        composable(route = OnboardingRoutes.CREATE_WALLET) {
            CreateWalletScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVerifyMnemonic = { walletId ->
                    navController.navigate(OnboardingRoutes.verifyMnemonic(walletId))
                },
            )
        }

        composable(
            route = OnboardingRoutes.VERIFY_MNEMONIC,
            arguments = listOf(
                navArgument("walletId") { type = NavType.StringType }
            ),
        ) {
            VerifyMnemonicScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSetPin = {
                    navController.navigate(OnboardingRoutes.SET_PIN) {
                        popUpTo(OnboardingRoutes.WELCOME) {
                            inclusive = false
                        }
                    }
                },
            )
        }

        composable(route = OnboardingRoutes.IMPORT_WALLET) {
            PlaceholderScreen(message = "Import Wallet — Coming Next")
        }

        composable(route = OnboardingRoutes.SET_PIN) {
            PlaceholderScreen(message = "Set PIN — Coming Next")
        }
    }
}

@Composable
private fun PlaceholderScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message)
    }
}
