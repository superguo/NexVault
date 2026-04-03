package com.nexvault.wallet.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Small badge showing the current blockchain network.
 *
 * Used on the Receive screen and transaction detail to indicate
 * which network an address or transaction belongs to.
 *
 * Ref: doc/03-UI-UX-DESIGN.md Section 1.3 — ChainBadge component
 *
 * @param chainName Display name of the chain
 * @param chainIconRes Drawable resource for the chain icon
 * @param isTestnet Whether to show a testnet indicator
 * @param modifier Optional modifier
 */
@Composable
fun ChainBadge(
    chainName: String,
    chainIconRes: Int,
    isTestnet: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        if (chainIconRes != 0) {
            Image(
                painter = painterResource(id = chainIconRes),
                contentDescription = chainName,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = if (isTestnet) "$chainName (Testnet)" else chainName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}