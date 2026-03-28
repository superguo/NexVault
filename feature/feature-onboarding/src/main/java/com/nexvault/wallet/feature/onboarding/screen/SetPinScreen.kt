package com.nexvault.wallet.feature.onboarding.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexvault.wallet.core.ui.components.NexVaultTopBar
import com.nexvault.wallet.core.ui.components.PinInputField
import com.nexvault.wallet.core.ui.theme.NexVaultTheme
import com.nexvault.wallet.feature.onboarding.viewmodel.SetPinViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Set PIN screen — final step of onboarding.
 * Two-phase: "Set Your PIN" then "Confirm Your PIN".
 * Optional biometric toggle at the bottom.
 *
 * Wireframe reference: doc/03-UI-UX-DESIGN.md Section 2.1 — SetPinScreen
 *
 * Layout:
 * - Top app bar with "Step 3 of 3" or "Set PIN"
 * - Title changes based on phase: "Set Your PIN" / "Confirm Your PIN"
 * - Subtitle with instructions
 * - PinInputField — 6-dot display + numeric keypad (from core-ui)
 * - Error message on mismatch
 * - Biometric toggle (only if device supports it)
 * - Loading overlay during PIN storage
 *
 * Uses key(uiState.phase) to reset PinInputField when phase changes.
 * Shake animation is applied to the dot row on PIN mismatch.
 */
@Composable
fun SetPinScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    showStepIndicator: Boolean = true,
    viewModel: SetPinViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                SetPinViewModel.NavigationEvent.NavigateToMain ->
                    onNavigateToMain()
            }
        }
    }

    SetPinScreenContent(
        uiState = uiState,
        showStepIndicator = showStepIndicator,
        onNavigateBack = onNavigateBack,
        onDigitPressed = viewModel::onDigitPressed,
        onBackspacePressed = viewModel::onBackspacePressed,
        onBiometricToggled = viewModel::onBiometricToggled,
        onShakeAnimationComplete = viewModel::onShakeAnimationComplete,
    )
}

@Composable
private fun SetPinScreenContent(
    uiState: SetPinViewModel.UiState,
    showStepIndicator: Boolean,
    onNavigateBack: () -> Unit,
    onDigitPressed: (Int) -> Unit,
    onBackspacePressed: () -> Unit,
    onBiometricToggled: (Boolean) -> Unit,
    onShakeAnimationComplete: () -> Unit,
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(uiState.isShakeError) {
        if (uiState.isShakeError) {
            repeat(3) {
                shakeOffset.animateTo(
                    targetValue = 12f,
                    animationSpec = tween(durationMillis = 50),
                )
                shakeOffset.animateTo(
                    targetValue = -12f,
                    animationSpec = tween(durationMillis = 50),
                )
            }
            shakeOffset.animateTo(0f, animationSpec = tween(durationMillis = 50))
            onShakeAnimationComplete()
        }
    }

    Scaffold(
        topBar = {
            NexVaultTopBar(
                title = if (showStepIndicator) "Step 3 of 3" else "Set PIN",
                showBackButton = uiState.phase == SetPinViewModel.PinPhase.SET,
                onBackClick = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = when (uiState.phase) {
                        SetPinViewModel.PinPhase.SET -> "Set Your PIN"
                        SetPinViewModel.PinPhase.CONFIRM -> "Confirm Your PIN"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (uiState.phase) {
                        SetPinViewModel.PinPhase.SET ->
                            "Choose a 6-digit PIN to secure your wallet."
                        SetPinViewModel.PinPhase.CONFIRM ->
                            "Enter the same PIN again to confirm."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }

                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = shakeOffset.value
                    },
                ) {
                    key(uiState.phase) {
                        PinInputField(
                            onDigitClick = onDigitPressed,
                            onBackspaceClick = onBackspacePressed,
                            filledCount = uiState.pin.length,
                            isError = uiState.isShakeError,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (uiState.isBiometricAvailable && uiState.phase == SetPinViewModel.PinPhase.SET) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Enable Biometric Unlock",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Switch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = onBiometricToggled,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Securing your wallet...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetPinScreenSetPhasePreview() {
    NexVaultTheme {
        SetPinScreenContent(
            uiState = SetPinViewModel.UiState(
                phase = SetPinViewModel.PinPhase.SET,
                pin = "123",
                isBiometricAvailable = true,
                isBiometricEnabled = false,
            ),
            showStepIndicator = true,
            onNavigateBack = {},
            onDigitPressed = {},
            onBackspacePressed = {},
            onBiometricToggled = {},
            onShakeAnimationComplete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SetPinScreenConfirmPhasePreview() {
    NexVaultTheme {
        SetPinScreenContent(
            uiState = SetPinViewModel.UiState(
                phase = SetPinViewModel.PinPhase.CONFIRM,
                pin = "",
                isBiometricAvailable = true,
                isBiometricEnabled = true,
            ),
            showStepIndicator = true,
            onNavigateBack = {},
            onDigitPressed = {},
            onBackspacePressed = {},
            onBiometricToggled = {},
            onShakeAnimationComplete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SetPinScreenErrorPreview() {
    NexVaultTheme {
        SetPinScreenContent(
            uiState = SetPinViewModel.UiState(
                phase = SetPinViewModel.PinPhase.SET,
                pin = "",
                error = "PINs don't match. Please try again.",
                isBiometricAvailable = false,
            ),
            showStepIndicator = false,
            onNavigateBack = {},
            onDigitPressed = {},
            onBackspacePressed = {},
            onBiometricToggled = {},
            onShakeAnimationComplete = {},
        )
    }
}
