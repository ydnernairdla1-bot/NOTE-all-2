package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.FinanceChartBucket
import com.example.ui.FinancePeriod
import com.example.ui.FinanceUiState
import com.example.ui.buildFinanceChartBuckets
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.utils.formatRupiah
import java.util.Locale

private enum class ChartDisplayMode {
    BAR,
    DONUT
}

private val ChartIncomeColor = Color(0xFF10B981)
private val ChartExpenseColor = Color(0xFFEF4444)
private val CategoryDonutPalette = listOf(
    Color(0xFF0EA5E9),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFF14B8A6)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceChartSheet(
    uiState: FinanceUiState,
    onPeriodSelected: (FinancePeriod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    var chartMode by remember { mutableStateOf(ChartDisplayMode.BAR) }
    val buckets = remember(uiState.period, uiState.periodTransactions, language, strings.chartWeekPrefix) {
        buildFinanceChartBuckets(
            period = uiState.period,
            transactions = uiState.periodTransactions,
            locale = language.locale,
            weekPrefix = strings.chartWeekPrefix
        )
    }

    var selectedBucketIndex by remember(buckets) {
        val lastActive = buckets.indexOfLast { it.income > 0L || it.expense > 0L }
        mutableIntStateOf(if (lastActive >= 0) lastActive else (buckets.lastIndex.coerceAtLeast(0)))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.financeChartTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.financeChartSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Period Chips + Chart Mode Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FinancePeriod.entries) { period ->
                    FilterChip(
                        selected = uiState.period == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(strings.financePeriodLabel(period)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = chartMode == ChartDisplayMode.BAR,
                    onClick = { chartMode = ChartDisplayMode.BAR },
                    label = { Text(strings.chartBarModeLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("chart_mode_bar")
                )
                FilterChip(
                    selected = chartMode == ChartDisplayMode.DONUT,
                    onClick = { chartMode = ChartDisplayMode.DONUT },
                    label = { Text(strings.chartDonutModeLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.DonutLarge,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("chart_mode_donut")
                )
            }

            // Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMiniStat(
                    label = strings.incomeLabel,
                    amount = formatRupiah(uiState.totalIncome),
                    dotColor = ChartIncomeColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryMiniStat(
                    label = strings.expenseLabel,
                    amount = formatRupiah(uiState.totalExpense),
                    dotColor = ChartExpenseColor,
                    modifier = Modifier.weight(1f)
                )
            }

            val hasData = uiState.totalIncome > 0L || uiState.totalExpense > 0L
            if (!hasData) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = strings.chartEmptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (chartMode == ChartDisplayMode.BAR) {
                IncomeExpenseBarChartCard(
                    buckets = buckets,
                    selectedIndex = selectedBucketIndex.coerceIn(0, (buckets.size - 1).coerceAtLeast(0)),
                    onSelectIndex = { selectedBucketIndex = it },
                    incomeLabel = strings.incomeLabel,
                    expenseLabel = strings.expenseLabel,
                    netLabel = strings.netBalanceTitle,
                    tapHint = strings.chartTapHint
                )
            } else {
                ProportionDonutChartsSection(
                    uiState = uiState
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_finance_chart_button")
            ) {
                Text(strings.close)
            }
        }
    }
}

@Composable
private fun SummaryMiniStat(
    label: String,
    amount: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IncomeExpenseBarChartCard(
    buckets: List<FinanceChartBucket>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    incomeLabel: String,
    expenseLabel: String,
    netLabel: String,
    tapHint: String
) {
    var animTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(buckets) {
        animTriggered = true
    }
    val animProgress by animateFloatAsState(
        targetValue = if (animTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "barChartProgress"
    )

    val maxAmount = remember(buckets) {
        buckets.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    val highlightColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegendDot(color = ChartIncomeColor, label = incomeLabel)
                    ChartLegendDot(color = ChartExpenseColor, label = expenseLabel)
                }
                Text(
                    text = formatCompactRupiah(maxAmount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bar Chart Canvas
            if (buckets.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .pointerInput(buckets) {
                            detectTapGestures { offset ->
                                val count = buckets.size.coerceAtLeast(1)
                                val idx = ((offset.x / size.width) * count).toInt()
                                    .coerceIn(0, count - 1)
                                onSelectIndex(idx)
                            }
                        }
                ) {
                    val count = buckets.size
                    val groupWidth = size.width / count
                    val topPad = 8.dp.toPx()
                    val bottomPad = 4.dp.toPx()
                    val usableHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                    // Horizontal grid lines (4 steps)
                    for (i in 0..3) {
                        val y = topPad + (usableHeight * i / 3f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    buckets.forEachIndexed { idx, bucket ->
                        val groupStart = idx * groupWidth
                        if (idx == selectedIndex) {
                            drawRoundRect(
                                color = highlightColor,
                                topLeft = Offset(groupStart + 2.dp.toPx(), 0f),
                                size = Size((groupWidth - 4.dp.toPx()).coerceAtLeast(1f), size.height),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )
                        }

                        val barWidth = (groupWidth * 0.28f).coerceIn(6.dp.toPx(), 20.dp.toPx())
                        val gapBetweenBars = 3.dp.toPx()
                        val pairTotalWidth = barWidth * 2f + gapBetweenBars
                        val pairStartX = groupStart + (groupWidth - pairTotalWidth) / 2f

                        val incomeFraction = (bucket.income.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
                        val incomeHeight = if (bucket.income > 0L) {
                            (usableHeight * incomeFraction * animProgress).coerceAtLeast(4.dp.toPx())
                        } else {
                            2.dp.toPx()
                        }
                        drawRoundRect(
                            color = if (bucket.income > 0L) ChartIncomeColor else ChartIncomeColor.copy(alpha = 0.2f),
                            topLeft = Offset(pairStartX, topPad + usableHeight - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        val expenseFraction = (bucket.expense.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
                        val expenseHeight = if (bucket.expense > 0L) {
                            (usableHeight * expenseFraction * animProgress).coerceAtLeast(4.dp.toPx())
                        } else {
                            2.dp.toPx()
                        }
                        drawRoundRect(
                            color = if (bucket.expense > 0L) ChartExpenseColor else ChartExpenseColor.copy(alpha = 0.2f),
                            topLeft = Offset(pairStartX + barWidth + gapBetweenBars, topPad + usableHeight - expenseHeight),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                // X-axis labels row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    buckets.forEachIndexed { idx, bucket ->
                        val isSelected = idx == selectedIndex
                        Text(
                            text = bucket.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectIndex(idx) }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                // Selected bucket detail pill
                val selected = buckets[selectedIndex]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selected.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$incomeLabel: ${formatRupiah(selected.income)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChartIncomeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$expenseLabel: ${formatRupiah(selected.expense)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ChartExpenseColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "$netLabel: ${formatRupiah(selected.income - selected.expense)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = tapHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProportionDonutChartsSection(
    uiState: FinanceUiState
) {
    val strings = LocalAppStrings.current
    val totalFlow = (uiState.totalIncome + uiState.totalExpense).coerceAtLeast(1L)
    val incomePct = ((uiState.totalIncome.toFloat() / totalFlow.toFloat()) * 100f).toInt()
    val expensePct = if (uiState.totalExpense > 0L) (100 - incomePct) else 0

    var animTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.totalIncome, uiState.totalExpense) {
        animTriggered = true
    }
    val sweepScale by animateFloatAsState(
        targetValue = if (animTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "donutSweep"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Card 1: Income vs Expense Donut
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.chartCashflowShareTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(132.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokePx = 18.dp.toPx()
                            val diameter = (minOf(size.width, size.height) - strokePx).coerceAtLeast(1f)
                            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                            val arcSize = Size(diameter, diameter)

                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx)
                            )

                            val incomeAngle = (uiState.totalIncome.toFloat() / totalFlow.toFloat()) * 360f * sweepScale
                            val expenseAngle = (uiState.totalExpense.toFloat() / totalFlow.toFloat()) * 360f * sweepScale

                            if (incomeAngle > 0f) {
                                drawArc(
                                    color = ChartIncomeColor,
                                    startAngle = -90f,
                                    sweepAngle = incomeAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                                )
                            }
                            if (expenseAngle > 0f) {
                                drawArc(
                                    color = ChartExpenseColor,
                                    startAngle = -90f + incomeAngle,
                                    sweepAngle = expenseAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = strings.chartTotalLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCompactRupiah(uiState.totalIncome + uiState.totalExpense),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DonutLegendRow(
                            color = ChartIncomeColor,
                            label = strings.incomeLabel,
                            value = "${formatCompactRupiah(uiState.totalIncome)} ($incomePct%)"
                        )
                        DonutLegendRow(
                            color = ChartExpenseColor,
                            label = strings.expenseLabel,
                            value = "${formatCompactRupiah(uiState.totalExpense)} ($expensePct%)"
                        )
                    }
                }
            }
        }

        // Card 2: Expense Category Donut
        if (uiState.expenseBreakdown.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.chartTopExpensesTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(132.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokePx = 18.dp.toPx()
                                val diameter = (minOf(size.width, size.height) - strokePx).coerceAtLeast(1f)
                                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                                val arcSize = Size(diameter, diameter)

                                drawArc(
                                    color = trackColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx)
                                )

                                var currentAngle = -90f
                                uiState.expenseBreakdown.forEachIndexed { idx, item ->
                                    val sweep = item.fraction * 360f * sweepScale
                                    if (sweep > 0f) {
                                        drawArc(
                                            color = CategoryDonutPalette[idx % CategoryDonutPalette.size],
                                            startAngle = currentAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            topLeft = topLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                                        )
                                        currentAngle += sweep
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = strings.expenseLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCompactRupiah(uiState.totalExpense),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.expenseBreakdown.forEachIndexed { idx, item ->
                                DonutLegendRow(
                                    color = CategoryDonutPalette[idx % CategoryDonutPalette.size],
                                    label = strings.categoryLabel(item.category),
                                    value = "${(item.fraction * 100).toInt()}%"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DonutLegendRow(
    color: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatCompactRupiah(amount: Long): String {
    val absVal = kotlin.math.abs(amount)
    val prefix = if (amount < 0) "-Rp " else "Rp "
    return when {
        absVal >= 1_000_000_000L -> String.format(Locale.US, "%s%.1fM", prefix, absVal / 1_000_000_000.0)
        absVal >= 1_000_000L -> String.format(Locale.US, "%s%.1fjt", prefix, absVal / 1_000_000.0)
        absVal >= 1_000L -> String.format(Locale.US, "%s%.0frb", prefix, absVal / 1_000.0)
        else -> "$prefix$absVal"
    }
}
