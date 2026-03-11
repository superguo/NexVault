package com.nexvault.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexvault.wallet.core.ui.components.GradientBackground
import com.nexvault.wallet.core.ui.components.NexVaultButton
import com.nexvault.wallet.core.ui.components.NexVaultCard
import com.nexvault.wallet.core.ui.components.PriceChangeChip
import com.nexvault.wallet.core.ui.components.ShimmerPlaceholder
import com.nexvault.wallet.core.ui.theme.NexVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexVaultTheme(darkTheme = true) {
                ThemePreviewScreen()
            }
        }
    }
}

@Composable
private fun ThemePreviewScreen() {
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            NexVaultCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "NexVault Wallet",
                        style = MaterialTheme.typography.displayLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NexVaultButton(
                text = "Get Started",
                onClick = { },
            )

            Spacer(modifier = Modifier.height(16.dp))

            PriceChangeChip(
                percentage = "+5.24%",
                isPositive = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShimmerPlaceholder(
                width = 200.dp,
                height = 20.dp,
            )
        }
    }
}
