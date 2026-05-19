package com.splitmate.android.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitmate.android.domain.model.GroupBudget

@Composable
fun BudgetProgressBar(budget: GroupBudget, totalSpend: Double) {
    val percent = if (budget.totalAmount > 0) (totalSpend / budget.totalAmount).coerceIn(0.0, 1.0) else 0.0
    val color = when {
        percent >= 1.0  -> Color(0xFFF43660)  // red
        percent >= 0.8  -> Color(0xFFF59E0B)  // amber
        else            -> Color(0xFF00D4AA)  // green
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Budget", fontSize = 12.sp, color = Color(0xFF64748B))
            Text(
                "₹${totalSpend.toInt()} / ₹${budget.totalAmount.toInt()}",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent.toFloat() },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E2D3D)
        )
        if (percent >= 1.0) {
            Text(
                "Over budget by ₹${(totalSpend - budget.totalAmount).toInt()}",
                fontSize = 11.sp, color = Color(0xFFF43660),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}