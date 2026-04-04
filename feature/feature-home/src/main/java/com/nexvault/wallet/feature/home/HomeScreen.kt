package com.nexvault.wallet.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexvault.wallet.core.ui.components.ChainSelectorDropdown
import com.nexvault.wallet.core.ui.components.NexVaultCard
import com.nexvault.wallet.core.ui.components.TokenIcon
import com.nexvault.wallet.core.ui.mapper.toChainUi
import com.nexvault.wallet.domain.model.chain.Chain
import com.nexvault.wallet.domain.model.token.PricePoint
import com.nexvault.wallet.domain.model.token.Token
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Home dashboard: portfolio value, chart, token list, and quick actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTokenDetail: (contractAddress: String, chainId: Int) -> Unit,
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToSwap: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        val msg = uiState.error
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onErrorDismissed()
        }
    }

    if (uiState.showAddTokenDialog) {
        AddTokenDialog(
            isLoading = uiState.addTokenLoading,
            error = uiState.addTokenError,
            onConfirm = { viewModel.onAddCustomToken(it) },
            onDismiss = { viewModel.onDismissAddTokenDialog() },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item(key = "chain_selector") {
                    ChainSelectorHeader(
                        selectedChain = uiState.selectedChain,
                        supportedChains = uiState.supportedChains,
                        onChainSelected = { viewModel.onChainSelected(it) },
                    )
                }
                item(key = "balance") {
                    PortfolioBalanceSection(
                        totalFiatValue = uiState.totalFiatValue,
                        change24hPercent = uiState.change24hPercent,
                        isLoading = uiState.isLoading,
                    )
                }
                item(key = "chart") {
                    PortfolioChartSection(
                        chartData = uiState.chartData,
                        selectedDays = uiState.selectedChartDays,
                        onRangeSelected = { viewModel.onChartRangeSelected(it) },
                    )
                }
                item(key = "actions") {
                    QuickActionsRow(
                        onSend = onNavigateToSend,
                        onReceive = onNavigateToReceive,
                        onSwap = onNavigateToSwap,
                    )
                }
                item(key = "tokens_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Tokens",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        TextButton(onClick = { viewModel.onShowAddTokenDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add token",
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Token")
                        }
                    }
                }
                if (uiState.isLoading && uiState.tokens.isEmpty()) {
                    items(5, key = { "shimmer_$it" }) {
                        TokenRowShimmer()
                    }
                }
                items(
                    items = uiState.tokens,
                    key = { "${it.contractAddress}-${it.chainId}" },
                ) { token ->
                    TokenRow(
                        token = token,
                        onClick = {
                            onNavigateToTokenDetail(token.contractAddress, token.chainId)
                        },
                    )
                }
                if (!uiState.isLoading && uiState.tokens.isEmpty()) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No tokens yet. Pull to refresh.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainSelectorHeader(
    selectedChain: Chain?,
    supportedChains: List<Chain>,
    onChainSelected: (Int) -> Unit,
) {
    val chain = selectedChain ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ChainSelectorDropdown(
            selectedChain = chain.toChainUi(),
            supportedChains = supportedChains.map { it.toChainUi() },
            onChainSelected = onChainSelected,
        )
    }
}

@Composable
private fun PortfolioBalanceSection(
    totalFiatValue: Double,
    change24hPercent: Double,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Total Balance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isLoading && totalFiatValue == 0.0) {
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(40.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        } else {
            Text(
                text = formatFiatValue(totalFiatValue),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val changeColor = when {
            change24hPercent > 0 -> MaterialTheme.colorScheme.primary
            change24hPercent < 0 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        val changePrefix = if (change24hPercent > 0) "+" else ""
        Text(
            text = "${changePrefix}${String.format("%.2f", change24hPercent)}% (24h)",
            style = MaterialTheme.typography.bodyMedium,
            color = changeColor,
        )
    }
}

@Composable
private fun PortfolioChartSection(
    chartData: List<PricePoint>,
    selectedDays: Int,
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
                .height(180.dp),
        ) {
            if (chartData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Loading chart...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                SimpleLineChart(
                    dataPoints = chartData,
                    modifier = Modifier.fillMaxSize(),
                    lineColor = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val ranges = listOf(1 to "1D", 7 to "7D", 30 to "1M", 90 to "3M", 365 to "ALL")
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

/**
 * Simple line chart drawn on Canvas.
 */
@Composable
fun SimpleLineChart(
    dataPoints: List<PricePoint>,
    modifier: Modifier = Modifier,
    lineColor: Color,
) {
    if (dataPoints.size < 2) return

    val minValue = dataPoints.minOf { it.value }
    val maxValue = dataPoints.maxOf { it.value }
    val valueRange = (maxValue - minValue).coerceAtLeast(0.01)

    Canvas(modifier = modifier.padding(8.dp)) {
        val stepX = size.width / (dataPoints.size - 1)
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / valueRange * size.height).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun QuickActionsRow(
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onSwap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuickActionButton(
            icon = Icons.Default.ArrowUpward,
            label = "Send",
            onClick = onSend,
        )
        QuickActionButton(
            icon = Icons.Default.ArrowDownward,
            label = "Receive",
            onClick = onReceive,
        )
        QuickActionButton(
            icon = Icons.Default.SwapHoriz,
            label = "Swap",
            onClick = onSwap,
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun TokenRow(
    token: Token,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TokenIcon(
            imageUrl = token.logoUrl,
            symbol = token.symbol,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = token.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = token.symbol,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${formatTokenBalance(token.balance)} ${token.symbol}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatFiatValue(token.fiatValue ?: 0.0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                token.priceChange24h?.let { change ->
                    Spacer(modifier = Modifier.width(4.dp))
                    val changeColor = if (change >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    val prefix = if (change >= 0) "+" else ""
                    Text(
                        text = "${prefix}${String.format("%.1f", change)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenRowShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

private fun formatFiatValue(value: Double): String {
    return "$${String.format("%,.2f", value)}"
}

private fun formatTokenBalance(balance: BigDecimal): String {
    return if (balance.scale() > 6) {
        balance.setScale(6, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    } else {
        balance.stripTrailingZeros().toPlainString()
    }
}
