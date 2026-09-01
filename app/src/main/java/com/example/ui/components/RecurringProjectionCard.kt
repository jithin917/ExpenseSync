package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CashProjectionSummary
import com.example.data.model.RecurringDebitType
import com.example.data.model.UpcomingDebitItem
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenSubtext
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringProjectionCard(
    projection: CashProjectionSummary,
    currencySymbol: String = "₹",
    onLookaheadChange: (Int) -> Unit,
    onSendNotificationClick: () -> Unit,
    onPayDebitClick: (UpcomingDebitItem) -> Unit,
    onDirectRecordClick: (UpcomingDebitItem) -> Unit,
    onManageRecurringClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var selectedFilterType by remember { mutableStateOf<RecurringDebitType?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recurring_projection_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PolishBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header row with Badge & Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Projection",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "LIQUIDITY & DEBIT PROJECTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Upcoming Recurring Obligations",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSendNotificationClick,
                        modifier = Modifier.size(34.dp).testTag("trigger_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Notification",
                            tint = BrandGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = PolishTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Cash Required Highlight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFFBEB))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED CASH REQUIRED IN NEXT ${projection.lookaheadDays} DAYS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatNumber(projection.totalRequiredAmount, currencySymbol),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF78350F)
                            )
                        }

                        if (projection.urgentCount > 0) {
                            Surface(
                                color = ExpenseRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed)
                                    )
                                    Text(
                                        text = "${projection.urgentCount} Due Soon",
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lookahead Period Chips (Configurable 5, 7, 10, 15, 30 days)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horizon:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )

                        listOf(5, 7, 10, 15, 30).forEach { days ->
                            val isSelected = projection.lookaheadDays == days
                            Surface(
                                color = if (isSelected) Color(0xFFD97706) else Color.White,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFD97706) else Color(0xFFFDE68A)
                                ),
                                modifier = Modifier
                                    .clickable { onLookaheadChange(days) }
                                    .testTag("horizon_${days}d_chip")
                            ) {
                                Text(
                                    text = "$days Days",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF78350F),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Projection Category Summary Cards (Salary, Rent, Utilities, etc.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Salary Projection Pill
                ProjectionCategoryPill(
                    title = "Salaries & Wages",
                    amount = projection.salaryTotal,
                    currencySymbol = currencySymbol,
                    icon = Icons.Default.Payments,
                    color = Color(0xFF16A34A),
                    bgColor = Color(0xFFDCFCE7),
                    isSelected = selectedFilterType == RecurringDebitType.SALARY,
                    onClick = {
                        selectedFilterType = if (selectedFilterType == RecurringDebitType.SALARY) null else RecurringDebitType.SALARY
                    }
                )

                // Rent Projection Pill
                ProjectionCategoryPill(
                    title = "Rent & Lease",
                    amount = projection.rentTotal,
                    currencySymbol = currencySymbol,
                    icon = Icons.Default.Hotel,
                    color = Color(0xFFEA580C),
                    bgColor = Color(0xFFFFEDD5),
                    isSelected = selectedFilterType == RecurringDebitType.RENT,
                    onClick = {
                        selectedFilterType = if (selectedFilterType == RecurringDebitType.RENT) null else RecurringDebitType.RENT
                    }
                )

                // Electricity / Utilities Pill
                ProjectionCategoryPill(
                    title = "Power & Utilities",
                    amount = projection.utilitiesTotal + projection.subscriptionsTotal,
                    currencySymbol = currencySymbol,
                    icon = Icons.Default.Bolt,
                    color = Color(0xFFCA8A04),
                    bgColor = Color(0xFFFEF9C3),
                    isSelected = selectedFilterType == RecurringDebitType.UTILITY,
                    onClick = {
                        selectedFilterType = if (selectedFilterType == RecurringDebitType.UTILITY) null else RecurringDebitType.UTILITY
                    }
                )

                if (projection.otherTotal > 0) {
                    ProjectionCategoryPill(
                        title = "Other Retainers",
                        amount = projection.otherTotal,
                        currencySymbol = currencySymbol,
                        icon = Icons.Default.Inventory,
                        color = Color(0xFF0D9488),
                        bgColor = Color(0xFFCCFBF1),
                        isSelected = selectedFilterType == RecurringDebitType.OTHER,
                        onClick = {
                            selectedFilterType = if (selectedFilterType == RecurringDebitType.OTHER) null else RecurringDebitType.OTHER
                        }
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    val displayedDebits = projection.allDebits.filter { item ->
                        selectedFilterType == null || item.recurringType == selectedFilterType
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedFilterType != null) {
                                "${selectedFilterType?.title} (${displayedDebits.size})"
                            } else {
                                "All Scheduled Debits (${displayedDebits.size})"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )

                        TextButton(
                            onClick = onManageRecurringClick,
                            modifier = Modifier.testTag("manage_recurring_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Manage",
                                modifier = Modifier.size(14.dp),
                                tint = BrandGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Manage & Add",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (displayedDebits.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PolishBackground)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recurring debits found for this filter.",
                                fontSize = 12.sp,
                                color = PolishTextMuted
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayedDebits.forEach { debit ->
                                UpcomingDebitRowItem(
                                    debit = debit,
                                    currencySymbol = currencySymbol,
                                    onPreFillClaim = { onPayDebitClick(debit) },
                                    onDirectPay = { onDirectRecordClick(debit) }
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
fun ProjectionCategoryPill(
    title: String,
    amount: Double,
    currencySymbol: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) color else bgColor,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color.White else color,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else PolishTextSecondary
                )
                Text(
                    text = formatNumber(amount, currencySymbol),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else color
                )
            }
        }
    }
}

@Composable
fun UpcomingDebitRowItem(
    debit: UpcomingDebitItem,
    currencySymbol: String,
    onPreFillClaim: () -> Unit,
    onDirectPay: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    val dueDateStr = sdf.format(Date(debit.nextDueDate))

    val statusBg = when {
        debit.isPaidInCurrentCycle -> Color(0xFFDCFCE7)
        debit.daysRemaining < 0 -> Color(0xFFFEE2E2)
        debit.daysRemaining <= 1 -> Color(0xFFFEF3C7)
        debit.daysRemaining <= 3 -> Color(0xFFFEF9C3)
        else -> PolishBackground
    }

    val statusColor = when {
        debit.isPaidInCurrentCycle -> Color(0xFF16A34A)
        debit.daysRemaining < 0 -> ExpenseRed
        debit.daysRemaining <= 1 -> Color(0xFFD97706)
        debit.daysRemaining <= 3 -> Color(0xFFCA8A04)
        else -> PolishTextSecondary
    }

    Surface(
        color = PolishBackground,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Type Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (debit.recurringType) {
                        RecurringDebitType.SALARY -> Icons.Default.Payments
                        RecurringDebitType.RENT -> Icons.Default.Hotel
                        RecurringDebitType.UTILITY -> Icons.Default.Bolt
                        RecurringDebitType.SUBSCRIPTION -> Icons.Default.Wifi
                        RecurringDebitType.EMI -> Icons.Default.AccountBalance
                        RecurringDebitType.VENDOR -> Icons.Default.Inventory
                        RecurringDebitType.OTHER -> Icons.Default.Schedule
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = debit.name,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = debit.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PolishTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Due Day: ${debit.dueDayOfMonth}th ($dueDateStr)",
                            fontSize = 11.sp,
                            color = PolishTextMuted
                        )

                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = debit.statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Amount & Quick Action Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatNumber(debit.amount, currencySymbol),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )

                if (debit.isPaidInCurrentCycle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Paid",
                            tint = BrandGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Cleared",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGreen
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            color = BrandGreenLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onPreFillClaim() }
                        ) {
                            Text(
                                text = "Claim",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = BrandGreen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onDirectPay() }
                        ) {
                            Text(
                                text = "1-Tap Settle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatNumber(amt: Double, sym: String): String {
    return if (amt % 1.0 == 0.0) "$sym${amt.toLong()}" else "$sym%.2f".format(amt)
}
