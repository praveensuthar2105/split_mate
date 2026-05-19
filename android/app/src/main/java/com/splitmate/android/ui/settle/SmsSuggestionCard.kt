package com.splitmate.android.ui.settle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SmsSuggestionCard(
    settlement: SettlementTransaction,
    fromUserName: String,
    detectedAmount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF052016)),
        border = BorderStroke(1.dp, Color(0xFF00D4AA).copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Sms,
                    contentDescription = null,
                    tint = Color(0xFF00D4AA),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "UPI payment detected",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00D4AA),
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "We detected a credit of ₹${detectedAmount.toInt()}. " +
                "Did $fromUserName settle their ₹${settlement.amount.toInt()}?",
                fontSize = 13.sp, color = Color(0xFFCBD5E1)
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4AA))
                ) {
                    Text("Yes, mark settled", color = Color.Black, fontSize = 12.sp)
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}