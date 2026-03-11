package com.nexvault.wallet.core.ui.preview

import androidx.compose.runtime.Composable
import com.nexvault.wallet.core.ui.theme.NexVaultTheme

@Composable
fun ThemePreviewWrapper(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    NexVaultTheme(darkTheme = darkTheme) {
        content()
    }
}
