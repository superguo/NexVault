package com.nexvault.wallet.feature.tokens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexvault.wallet.core.ui.components.NexVaultCard
import com.nexvault.wallet.core.ui.components.SimpleLineChart
import com.nexvault.wallet.core.ui.components.TokenIcon
import com.nexvault.wallet.core.ui.components.TransactionRow
import com.nexvault.wallet.core.ui.util.formatFiatValue
import com.nexvault.wallet.core.ui.util.formatTokenBalance
import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.model.token.Token

/**
 * Token detail: balance, chart, stats, actions, and recent transactions.
 *
 * @param onNavigateBack Back navigation.
 * @param onNavigateToSend Opens send flow for this token (wired in a later task).
 * @param onNavigateToReceive Opens receive flow.
 * @param onNavigateToHistory Opens full history for this token filter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSend: (contractAddress: String, chainId: Int) -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToHistory: (contractAddress: String, chainId: Int) -> Unit,
    viewModel: TokenDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val token = uiState.token

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = token?.symbol ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState.isLoading && token == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                token != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        item(key = "header") {
                            TokenDetailHeader(token = token)
                        }
                        item(key = "chart") {
                            TokenPriceChartSection(
                                chartData = uiState.chartData,
                                selectedDays = uiState.selectedChartDays,
                                isLoading = uiState.isChartLoading,
                                onRangeSelected = { viewModel.onChartRangeSelected(it) },
                            )
                        }
                        item(key = "stats") {
                            TokenPriceStats(token = token)
                        }
                        item(key = "actions") {
                            TokenActionButtons(
                                onSend = { onNavigateToSend(token.contractAddress, token.chainId) },
                                onReceive = onNavigateToReceive,
                            )
                        }
                        if (uiState.recentTransactions.isNotEmpty()) {
                            item(key = "tx_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Recent Transactions",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    TextButton(
                                        onClick = {
                                            onNavigateToHistory(token.contractAddress, token.chainId)
                                        },
                                    ) {
                                        Text("See All")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "See all transactions",
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                            items(
                                items = uiState.recentTransactions,
                                key = { "${it.txHash}-${it.chainId}" },
                            ) { transaction ->
                                TransactionRow(
                                    transaction = transaction,
                                    tokenSymbol = token.symbol,
                                    tokenDecimals = token.decimals,
                                )
                            }
                        }
                        if (uiState.recentTransactions.isEmpty() && !uiState.isLoading) {
                            item(key = "no_tx") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No transactions yet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = uiState.error ?: "Unable to load token",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenDetailHeader(token: Token) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TokenIcon(
            imageUrl = token.logoUrl,
            symbol = token.symbol,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = token.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${formatTokenBalance(token.balance)} ${token.symbol}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatFiatValue(token.fiatValue ?: 0.0),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TokenPriceChartSection(
    chartData: List<PricePoint>,
    selectedDays: Int,
    isLoading: Boolean,
    onRangeSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        NexVaultCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                chartData.size < 2 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No chart data available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    SimpleLineChart(
                        dataPoints = chartData,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        lineColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val ranges = listOf(1 to "1D", 7 to "7D", 30 to "1M", 365 to "1Y")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ranges.forEach { (days, label) ->
                val isSelected = days == selectedDays
                FilterChip(
                    selected = isSelected,
                    onClick = { onRangeSelected(days) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun TokenPriceStats(token: Token) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        val fiatPrice = token.fiatPrice
        TokenStatRow(
            label = "Price",
            value = if (fiatPrice != null) formatFiatValue(fiatPrice) else "—",
        )
        val change24h = token.priceChange24h
        val changeColor = when {
            (change24h ?: 0.0) > 0 -> Color(0xFF4CAF50)
            (change24h ?: 0.0) < 0 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        val changePrefix = if ((change24h ?: 0.0) > 0) "+" else ""
        TokenStatRow(
            label = "24h Change",
            value = if (change24h != null) {
                "$changePrefix${String.format("%.2f", change24h)}%"
            } else {
                "—"
            },
            valueColor = changeColor,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun TokenStatRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}

@Composable
private fun TokenActionButtons(
    onSend: () -> Unit,
    onReceive: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onSend,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send")
        }
        OutlinedButton(
            onClick = onReceive,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Receive")
        }
    }
}
