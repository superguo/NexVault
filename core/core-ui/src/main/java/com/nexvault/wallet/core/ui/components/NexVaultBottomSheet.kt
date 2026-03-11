package com.nexvault.wallet.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexvault.wallet.core.ui.theme.NexVaultDimens
import com.nexvault.wallet.core.ui.theme.NexVaultTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexVaultBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable () -> Unit,
) {
    val colors = NexVaultTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = NexVaultDimens.cornerRadiusExtraLarge,
            topEnd = NexVaultDimens.cornerRadiusExtraLarge,
        ),
        containerColor = colors.cardSurface,
        contentColor = colors.textHigh,
    ) {
        content()
    }
}
