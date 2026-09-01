package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionWithDetails
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenSubtext
import com.example.ui.theme.BrandGreenTint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishSurface
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Vibrant, modern category color palette inspired by professional financial dashboards.
 */
val CategoryChartColors = listOf(
    Color(0xFF2E7D32), // Emerald Brand Green
    Color(0xFF0284C7), // Ocean Sky Blue
    Color(0xFF8B5CF6), // Royal Violet
    Color(0xFFF59E0B), // Warm Amber
    Color(0xFFE11D48), // Rose Red
    Color(0xFF0D9488), // Teal
    Color(0xFF6366F1), // Indigo
    Color(0xFFD97706), // Tangerine
    Color(0xFF475569), // Slate
    Color(0xFFEC4899)  // Pink
)

data class CategoryBreakdownItem(
    val categoryId: String?,
    val categoryName: String,
    val iconName: String,
    val colorHex: String,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int,
    val chartColor: Color
)

@Composable
fun MonthlyExpenseBreakdownCard(
    transactions: List<TransactionWithDetails>,
    categories: List<CategoryEntity>,
    currencySymbol: String = "₹",
    selectedCategoryFilter: String? = null,
    onCategoryClick: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Month offset relative to current month: 0 = current month, -1 = last month, etc.
    var monthOffset by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(true) }

    // Calculate month boundary timestamps
    val calendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
    }

    val monthStart = remember(monthOffset) {
        (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val monthEnd = remember(monthOffset) {
        (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val monthLabel = remember(monthOffset) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(Date(monthStart))
    }

    val isCurrentMonth = monthOffset == 0

    // Filter transactions for this month
    val monthTransactions = remember(transactions, monthStart, monthEnd) {
        transactions.filter { it.transaction.transactionDate in monthStart..monthEnd }
    }

    val totalMonthSpend = remember(monthTransactions) {
        monthTransactions.sumOf { it.transaction.amount }
    }

    // Group expenses by category
    val categoryBreakdowns = remember(monthTransactions, categories, totalMonthSpend) {
        val catMap = categories.associateBy { it.id }
        val grouped = monthTransactions.groupBy { it.transaction.categoryId }

        val items = mutableListOf<CategoryBreakdownItem>()
        var colorIdx = 0

        for ((catId, txList) in grouped) {
            val catTotal = txList.sumOf { it.transaction.amount }
            val catObj = catId?.let { catMap[it] }
            val name = catObj?.name ?: "Uncategorized"
            val icon = catObj?.iconName ?: "more"
            val hex = catObj?.colorHex ?: "#64748B"
            val pct = if (totalMonthSpend > 0) (catTotal / totalMonthSpend).toFloat() * 100f else 0f
            val chartColor = CategoryChartColors[colorIdx % CategoryChartColors.size]
            colorIdx++

            items.add(
                CategoryBreakdownItem(
                    categoryId = catId,
                    categoryName = name,
                    iconName = icon,
                    colorHex = hex,
                    amount = catTotal,
                    percentage = pct,
                    transactionCount = txList.size,
                    chartColor = chartColor
                )
            )
        }

        // Sort descending by amount
        items.sortedByDescending { it.amount }
    }

    val topCategory = categoryBreakdowns.firstOrNull()

    Card(
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PolishBorder, RoundedCornerShape(24.dp))
            .testTag("monthly_breakdown_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with Month Stepper Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandGreenTint)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Breakdown",
                            tint = BrandGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Category Breakdown",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "Monthly Spending Distribution",
                            fontSize = 11.sp,
                            color = PolishTextMuted
                        )
                    }
                }

                // Month Stepper Pill
                Surface(
                    color = PolishBackground,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { monthOffset -= 1 },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("breakdown_prev_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = PolishTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = if (isCurrentMonth) "This Month" else monthLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandGreenDark,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                if (monthOffset < 0) monthOffset += 1
                            },
                            enabled = monthOffset < 0,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("breakdown_next_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Month",
                                tint = if (monthOffset < 0) PolishTextSecondary else PolishTextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Total Spend & Key Stat Badges
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "TOTAL FOR ${monthLabel.uppercase(Locale.getDefault())}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishTextMuted,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatCurrency(totalMonthSpend, currencySymbol),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenDark
                    )
                }

                if (topCategory != null && totalMonthSpend > 0) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Top Spend",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Top: ${topCategory.categoryName} (%.0f%%)".format(topCategory.percentage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (categoryBreakdowns.isEmpty() || totalMonthSpend <= 0.0) {
                // Empty state for the selected month
                Surface(
                    color = PolishBackground,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "No expenses logged in $monthLabel",
                            fontSize = 13.sp,
                            color = PolishTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // 1. Stacked Multi-Segment Progress Bar (Recharts/Interactive style)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DISTRIBUTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Multi-segment horizontal stacked bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(PolishBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            categoryBreakdowns.forEach { item ->
                                val weight = item.percentage / 100f
                                if (weight > 0.001f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(weight.coerceAtLeast(0.01f))
                                            .background(item.chartColor)
                                            .clickable {
                                                onCategoryClick(if (selectedCategoryFilter == item.categoryId) null else item.categoryId)
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color Legend Chips (Scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryBreakdowns.forEach { item ->
                            val isSelected = selectedCategoryFilter == item.categoryId
                            Surface(
                                color = if (isSelected) item.chartColor.copy(alpha = 0.15f) else PolishBackground,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) item.chartColor else Color.Transparent
                                ),
                                modifier = Modifier.clickable {
                                    onCategoryClick(if (isSelected) null else item.categoryId)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(item.chartColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${item.categoryName} (${String.format(Locale.US, "%.0f%%", item.percentage)})",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PolishTextPrimary else PolishTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Individual Category Breakdown with Progress Bars
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categoryBreakdowns.forEachIndexed { index, item ->
                        val isSelected = selectedCategoryFilter == item.categoryId
                        val animatedProgress by animateFloatAsState(
                            targetValue = (item.percentage / 100f).coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 600, delayMillis = index * 50),
                            label = "cat_progress_${item.categoryId}"
                        )

                        Surface(
                            color = if (isSelected) BrandGreenTint.copy(alpha = 0.5f) else PolishBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BrandGreen else PolishBorder.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCategoryClick(if (isSelected) null else item.categoryId)
                                }
                                .testTag("breakdown_item_${item.categoryName.lowercase().replace(" ", "_")}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Category Dot / Small Icon Indicator
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(item.chartColor.copy(alpha = 0.15f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(item.chartColor)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = item.categoryName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = PolishTextPrimary
                                            )
                                            Text(
                                                text = "${item.transactionCount} ${if (item.transactionCount == 1) "claim" else "claims"}",
                                                fontSize = 10.sp,
                                                color = PolishTextMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCurrency(item.amount, currencySymbol),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PolishTextPrimary
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.1f%%", item.percentage),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = item.chartColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Modern Linear Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(PolishBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(animatedProgress)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(item.chartColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
