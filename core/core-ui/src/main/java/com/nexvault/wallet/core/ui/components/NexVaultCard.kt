package com.nexvault.wallet.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexvault.wallet.core.ui.theme.NexVaultDimens
import com.nexvault.wallet.core.ui.theme.NexVaultTheme

@Composable
fun NexVaultCard(
    modifier: Modifier = Modifier,
    useGradient: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = NexVaultTheme.colors

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(NexVaultDimens.cornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardSurface,
        ),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = NexVaultDimens.cardElevation),
    ) {
        Box(
            modifier = Modifier.padding(NexVaultDimens.spacingMd)
        ) {
            content()
        }
    }
}
