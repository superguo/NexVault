package com.nexvault.wallet.feature.onboarding.screen

import android.app.Activity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexvault.wallet.core.ui.components.NexVaultButton
import com.nexvault.wallet.core.ui.components.NexVaultTopBar
import com.nexvault.wallet.core.ui.theme.NexVaultTheme
import com.nexvault.wallet.feature.onboarding.viewmodel.CreateWalletViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Create Wallet screen — displays the generated 12-word mnemonic phrase.
 * User must write down the words and check the acknowledgment checkbox
 * before the Continue button is enabled.
 *
 * Wireframe reference: doc/03-UI-UX-DESIGN.md Section 2.1 — CreateWalletScreen
 *
 * Layout:
 * - Top app bar with back arrow and "Step 1 of 3"
 * - Title "Your Recovery Phrase"
 * - Subtitle warning about writing words down
 * - 4×3 grid of numbered mnemonic words
 * - Checkbox "I have written it down"
 * - Continue button (disabled until checkbox checked)
 *
 * FLAG_SECURE is set on this screen to prevent screenshots.
 */
@Composable
fun CreateWalletScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVerifyMnemonic: (String) -> Unit,
    viewModel: CreateWalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is CreateWalletViewModel.NavigationEvent.NavigateToVerifyMnemonic ->
                    onNavigateToVerifyMnemonic(event.walletId)
            }
        }
    }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
        )
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    CreateWalletScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAcknowledgeToggled = viewModel::onAcknowledgeToggled,
        onContinueClicked = viewModel::onContinueClicked,
        onRetryClicked = viewModel::onRetryClicked,
    )
}

@Composable
private fun CreateWalletScreenContent(
    uiState: CreateWalletViewModel.UiState,
    onNavigateBack: () -> Unit,
    onAcknowledgeToggled: (Boolean) -> Unit,
    onContinueClicked: () -> Unit,
    onRetryClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            NexVaultTopBar(
                title = "Step 1 of 3",
                showBackButton = true,
                onBackClick = onNavigateBack,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Creating your wallet...",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NexVaultButton(
                            text = "Retry",
                            onClick = onRetryClicked,
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Recovery Phrase",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Write these 12 words down in order. Never share them with anyone. This is the only way to recover your wallet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MnemonicGrid(
                        words = uiState.mnemonicWords,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = uiState.isAcknowledged,
                            onCheckedChange = onAcknowledgeToggled,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I have written down my recovery phrase",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    NexVaultButton(
                        text = "Continue",
                        onClick = onContinueClicked,
                        enabled = uiState.isAcknowledged,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Displays mnemonic words in a 4×3 grid (4 rows, 3 columns).
 * Each cell shows the word number and the word.
 */
@Composable
fun MnemonicGrid(
    words: List<String>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        itemsIndexed(words) { index, word ->
            MnemonicWordCell(
                number = index + 1,
                word = word,
            )
        }
    }
}

@Composable
private fun MnemonicWordCell(
    number: Int,
    word: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$number.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = word,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateWalletScreenPreview() {
    NexVaultTheme {
        CreateWalletScreenContent(
            uiState = CreateWalletViewModel.UiState(
                mnemonicWords = listOf(
                    "apple", "brave", "crane", "delta", "eagle", "frost",
                    "grape", "house", "ivory", "jump", "king", "lamp"
                ),
                walletId = "sample-id",
                address = "0x1234...abcd",
                isAcknowledged = false,
                isLoading = false,
                error = null,
            ),
            onNavigateBack = {},
            onAcknowledgeToggled = {},
            onContinueClicked = {},
            onRetryClicked = {},
        )
    }
}
