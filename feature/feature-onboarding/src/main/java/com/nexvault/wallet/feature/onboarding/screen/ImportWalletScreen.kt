package com.nexvault.wallet.feature.onboarding.screen

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexvault.wallet.core.ui.components.NexVaultButton
import com.nexvault.wallet.core.ui.components.NexVaultTopBar
import com.nexvault.wallet.core.ui.theme.NexVaultTheme
import com.nexvault.wallet.feature.onboarding.viewmodel.ImportWalletViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Import Wallet screen — allows importing via mnemonic phrase or private key.
 *
 * Wireframe reference: doc/03-UI-UX-DESIGN.md Section 2.1 — ImportWalletScreen
 *
 * Layout:
 * - Top app bar with back arrow
 * - Title "Import Wallet"
 * - Tab toggle for Mnemonic vs Private Key mode
 * - Mnemonic mode: large multiline text field for 12/24 word phrase
 * - Private Key mode: single-line text field for hex key
 * - Real-time validation with inline error messages
 * - Import button at bottom (disabled until input is valid format)
 * - Loading indicator when import is in progress
 *
 * Security: FLAG_SECURE is set to prevent screenshots of entered secrets.
 */
@Composable
fun ImportWalletScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSetPin: () -> Unit,
    viewModel: ImportWalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                ImportWalletViewModel.NavigationEvent.NavigateToSetPin ->
                    onNavigateToSetPin()
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

    ImportWalletScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onImportModeChanged = viewModel::onImportModeChanged,
        onMnemonicInputChanged = viewModel::onMnemonicInputChanged,
        onPrivateKeyInputChanged = viewModel::onPrivateKeyInputChanged,
        onImportClicked = viewModel::onImportClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportWalletScreenContent(
    uiState: ImportWalletViewModel.UiState,
    onNavigateBack: () -> Unit,
    onImportModeChanged: (ImportWalletViewModel.ImportMode) -> Unit,
    onMnemonicInputChanged: (String) -> Unit,
    onPrivateKeyInputChanged: (String) -> Unit,
    onImportClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            NexVaultTopBar(
                title = "Import Wallet",
                showBackButton = true,
                onBackClick = onNavigateBack,
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                ) {
                    if (uiState.generalError != null) {
                        Text(
                            text = uiState.generalError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    NexVaultButton(
                        text = if (uiState.isLoading) "Importing..." else "Import",
                        onClick = onImportClicked,
                        enabled = uiState.isImportEnabled && !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ImportModeSelector(
                selectedMode = uiState.importMode,
                onModeChanged = onImportModeChanged,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (uiState.importMode) {
                ImportWalletViewModel.ImportMode.MNEMONIC -> {
                    MnemonicInputSection(
                        input = uiState.mnemonicInput,
                        error = uiState.mnemonicError,
                        onInputChanged = onMnemonicInputChanged,
                        isLoading = uiState.isLoading,
                    )
                }
                ImportWalletViewModel.ImportMode.PRIVATE_KEY -> {
                    PrivateKeyInputSection(
                        input = uiState.privateKeyInput,
                        error = uiState.privateKeyError,
                        onInputChanged = onPrivateKeyInputChanged,
                        isLoading = uiState.isLoading,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImportModeSelector(
    selectedMode: ImportWalletViewModel.ImportMode,
    onModeChanged: (ImportWalletViewModel.ImportMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedTabIndex = when (selectedMode) {
        ImportWalletViewModel.ImportMode.MNEMONIC -> 0
        ImportWalletViewModel.ImportMode.PRIVATE_KEY -> 1
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
    ) {
        Tab(
            selected = selectedMode == ImportWalletViewModel.ImportMode.MNEMONIC,
            onClick = { onModeChanged(ImportWalletViewModel.ImportMode.MNEMONIC) },
            text = { Text("Recovery Phrase") },
        )
        Tab(
            selected = selectedMode == ImportWalletViewModel.ImportMode.PRIVATE_KEY,
            onClick = { onModeChanged(ImportWalletViewModel.ImportMode.PRIVATE_KEY) },
            text = { Text("Private Key") },
        )
    }
}

@Composable
private fun MnemonicInputSection(
    input: String,
    error: String?,
    onInputChanged: (String) -> Unit,
    isLoading: Boolean,
) {
    Text(
        text = "Recovery Phrase",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Enter your 12 or 24 word recovery phrase, with words separated by spaces.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = input,
        onValueChange = onInputChanged,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        enabled = !isLoading,
        placeholder = {
            Text(
                text = "word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 word11 word12",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            } else {
                val wordCount = input.trim()
                    .split("\\s+".toRegex())
                    .filter { it.isNotEmpty() }.size
                if (wordCount > 0) {
                    Text(
                        text = "$wordCount / 12 words",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            autoCorrectEnabled = false,
        ),
        maxLines = 6,
        shape = MaterialTheme.shapes.medium,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Never share your recovery phrase with anyone.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
    )
}

@Composable
private fun PrivateKeyInputSection(
    input: String,
    error: String?,
    onInputChanged: (String) -> Unit,
    isLoading: Boolean,
) {
    Text(
        text = "Private Key",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Enter your 64-character hexadecimal private key. Optionally prefixed with 0x.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = input,
        onValueChange = onInputChanged,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        placeholder = {
            Text(
                text = "0x or 64 hex characters",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            } else {
                val cleaned = input.trim().removePrefix("0x").removePrefix("0X")
                if (cleaned.isNotEmpty()) {
                    Text(
                        text = "${cleaned.length} / 64 characters",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done,
            autoCorrectEnabled = false,
        ),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Never share your private key with anyone.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
    )
}

@Preview(showBackground = true)
@Composable
private fun ImportWalletScreenMnemonicPreview() {
    NexVaultTheme {
        ImportWalletScreenContent(
            uiState = ImportWalletViewModel.UiState(
                importMode = ImportWalletViewModel.ImportMode.MNEMONIC,
                mnemonicInput = "apple brave crane delta eagle frost grape house ivory jump king",
                mnemonicError = null,
                isImportEnabled = false,
            ),
            onNavigateBack = {},
            onImportModeChanged = {},
            onMnemonicInputChanged = {},
            onPrivateKeyInputChanged = {},
            onImportClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportWalletScreenPrivateKeyPreview() {
    NexVaultTheme {
        ImportWalletScreenContent(
            uiState = ImportWalletViewModel.UiState(
                importMode = ImportWalletViewModel.ImportMode.PRIVATE_KEY,
                privateKeyInput = "0xabcdef1234567890",
                privateKeyError = null,
                isImportEnabled = false,
            ),
            onNavigateBack = {},
            onImportModeChanged = {},
            onMnemonicInputChanged = {},
            onPrivateKeyInputChanged = {},
            onImportClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportWalletScreenErrorPreview() {
    NexVaultTheme {
        ImportWalletScreenContent(
            uiState = ImportWalletViewModel.UiState(
                importMode = ImportWalletViewModel.ImportMode.MNEMONIC,
                mnemonicInput = "invalid words here that are not a real mnemonic phrase at all twelve",
                generalError = "Invalid mnemonic phrase. Please check your words.",
                isImportEnabled = true,
            ),
            onNavigateBack = {},
            onImportModeChanged = {},
            onMnemonicInputChanged = {},
            onPrivateKeyInputChanged = {},
            onImportClicked = {},
        )
    }
}
