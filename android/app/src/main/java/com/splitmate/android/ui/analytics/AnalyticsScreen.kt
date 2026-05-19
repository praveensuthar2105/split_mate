package com.splitmate.android.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.splitmate.android.domain.model.GroupAnalytics

@Composable
fun AnalyticsScreen(analytics: GroupAnalytics) {
    LazyColumn(Modifier.padding(16.dp)) {

        item {
            Text(
                "Monthly Spend",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))

            // Map data to entries for Vico
            val entries = analytics.monthlyTotals.values.mapIndexed { index, value ->
                FloatEntry(x = index.toFloat(), y = value.toFloat())
            }
            val chartEntryModelProducer = remember { ChartEntryModelProducer(entries) }

            // Update the entries if data changes
            androidx.compose.runtime.LaunchedEffect(analytics.monthlyTotals) {
                chartEntryModelProducer.setEntries(entries)
            }

            Chart(
                chart = columnChart(),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        item {
            Spacer(Modifier.height(32.dp))
            Text(
                "Spend by Category",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
        }

        items(analytics.categoryBreakdown.entries.toList().sortedByDescending { it.value }) { (category, amount) ->
            CategoryRow(
                category = category,
                amount = amount,
                total = analytics.totalSpend
            )
        }
    }
}

@Composable
fun CategoryRow(category: String, amount: Double, total: Double) {
    val percentage = if (total > 0) (amount / total) * 100 else 0.0
    val icon = getIconForCategory(category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF334155).copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${String.format("%.1f", percentage)}%",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Text(
            text = "₹${amount.toInt()}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// Helper to turn string categories into Emojis for the UI
private fun getIconForCategory(category: String): String {
    return when (category.lowercase()) {
        "food" -> "🍔"
        "transport" -> "🚕"
        "rent", "housing" -> "🏠"
        "groceries" -> "🛒"
        "entertainment" -> "🍿"
        "utilities" -> "⚡"
        else -> "💸"
    }
}