package com.nexvault.wallet.feature.onboarding.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexvault.wallet.core.ui.components.NexVaultButton
import com.nexvault.wallet.core.ui.theme.NexVaultTheme
import com.nexvault.wallet.feature.onboarding.viewmodel.WelcomeViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Welcome screen — the first screen users see.
 * Two primary actions: Create New Wallet or Import Wallet.
 *
 * Wireframe reference: doc/03-UI-UX-DESIGN.md Section 2.1 — WelcomeScreen
 *
 * Layout:
 * - Centered app logo/icon area (programmatic shield/vault icon)
 * - App name "NexVault"
 * - Tagline "Your keys. Your crypto."
 * - "Create New Wallet" primary button
 * - "Import Wallet" secondary button
 */
@Composable
fun WelcomeScreen(
    onNavigateToCreateWallet: () -> Unit,
    onNavigateToImportWallet: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                WelcomeViewModel.NavigationEvent.NavigateToCreateWallet ->
                    onNavigateToCreateWallet()
                WelcomeViewModel.NavigationEvent.NavigateToImportWallet ->
                    onNavigateToImportWallet()
            }
        }
    }

    WelcomeScreenContent(
        onCreateWalletClicked = viewModel::onCreateWalletClicked,
        onImportWalletClicked = viewModel::onImportWalletClicked,
    )
}

@Composable
private fun WelcomeScreenContent(
    onCreateWalletClicked: () -> Unit,
    onImportWalletClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        VaultIcon(
            modifier = Modifier.size(120.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "NexVault",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your keys. Your crypto.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        NexVaultButton(
            text = "Create New Wallet",
            onClick = onCreateWalletClicked,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        NexVaultButton(
            text = "Import Wallet",
            onClick = onImportWalletClicked,
            modifier = Modifier.fillMaxWidth(),
            secondary = true,
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

/**
 * Programmatic vault/shield icon for the welcome screen.
 * Uses Canvas to draw a shield shape with a lock icon inside.
 */
@Composable
private fun VaultIcon(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawShieldShape(primaryColor)
        }
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = onPrimaryColor,
        )
    }
}

private fun DrawScope.drawShieldShape(color: Color) {
    val path = Path().apply {
        moveTo(size.width / 2, 0f)
        lineTo(size.width, size.height * 0.25f)
        lineTo(size.width * 0.85f, size.height * 0.75f)
        lineTo(size.width / 2, size.height)
        lineTo(size.width * 0.15f, size.height * 0.75f)
        lineTo(0f, size.height * 0.25f)
        close()
    }
    drawPath(path = path, color = color)
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    NexVaultTheme {
        WelcomeScreenContent(
            onCreateWalletClicked = {},
            onImportWalletClicked = {},
        )
    }
}
