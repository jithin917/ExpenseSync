package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenLight
import com.example.ui.theme.BrandGreenTint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.PendingOrangeLight
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishHover
import com.example.ui.theme.PolishTextMuted
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isSynced = status == "SYNCED"
    val bgColor = if (isSynced) BrandGreenTint else PendingOrangeLight
    val contentColor = if (isSynced) BrandGreen else PendingOrange
    val label = if (isSynced) "Synced" else "Pending Sync"
    val icon = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Schedule

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PaymentSourcePill(
    type: String, // SB or CC
    displayLabel: String,
    modifier: Modifier = Modifier
) {
    val isCC = type == "CC"
    val bgColor = if (isCC) BrandGreenTint else PolishHover
    val contentColor = if (isCC) BrandGreen else PolishTextSecondary
    val icon = if (isCC) Icons.Default.CreditCard else Icons.Default.Payment

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayLabel,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val isRed = iconName.lowercase() in listOf("travel", "flight")
    val bgColor = if (isRed) ExpenseRedLight else BrandGreenTint
    val iconColor = if (isRed) ExpenseRed else BrandGreen

    val iconVector = getCategoryVectorIcon(iconName)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = iconName,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

fun getCategoryVectorIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "flight", "travel" -> Icons.Default.Flight
        "restaurant", "meals", "food" -> Icons.Default.Restaurant
        "inventory", "office", "supplies" -> Icons.Default.Inventory
        "hotel", "lodging" -> Icons.Default.Hotel
        "celebration", "client" -> Icons.Default.Star
        "terminal", "software" -> Icons.Default.Terminal
        "receipt" -> Icons.Default.Receipt
        else -> Icons.Default.MoreHoriz
    }
}

fun formatExpenseDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatExpenseDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatCurrency(amount: Double, symbol: String = "₹"): String {
    return "$symbol${String.format(Locale.US, "%,.2f", amount)}"
}

@Composable
fun ReceiptViewerDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val file = java.io.File(imagePath)
            coil.compose.AsyncImage(
                model = file,
                contentDescription = "Receipt Full View",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp)
            )

            androidx.compose.material3.IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.MoreHoriz,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

