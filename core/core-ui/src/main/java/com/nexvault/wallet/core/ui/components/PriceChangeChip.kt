package com.nexvault.wallet.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nexvault.wallet.core.ui.theme.NexVaultTheme

@Composable
fun PriceChangeChip(
    percentage: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = NexVaultTheme.colors
    val backgroundColor = if (isPositive) {
        colors.positive.copy(alpha = 0.15f)
    } else {
        colors.negative.copy(alpha = 0.15f)
    }
    val contentColor = if (isPositive) colors.positive else colors.negative

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isPositive) {
                Icons.Filled.KeyboardArrowUp
            } else {
                Icons.Filled.KeyboardArrowDown
            },
            contentDescription = if (isPositive) "Positive" else "Negative",
            modifier = Modifier.size(16.dp),
            tint = contentColor,
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
