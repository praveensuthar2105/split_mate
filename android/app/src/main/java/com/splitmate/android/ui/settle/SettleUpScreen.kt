package com.splitmate.android.ui.settle

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitmate.android.util.UpiDeepLink

data class SettlementTransaction(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val isSettled: Boolean
)

data class GroupMember(
    val id: String,
    val name: String,
    val upiId: String?
)

@Composable
fun SettleUpScreen(
    settlements: List<SettlementTransaction>,
    members: List<GroupMember>,
    onMarkSettled: (SettlementTransaction) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        item {
            AlgorithmInfoCard(
                memberCount = members.size,
                transactionCount = settlements.size
            )
        }

        items(settlements) { txn ->
            val fromMember = members.find { it.id == txn.fromUserId }
            val toMember = members.find { it.id == txn.toUserId }

            SettlementCard(
                from = fromMember,
                to = toMember,
                amount = txn.amount,
                isSettled = txn.isSettled,
                onPayUpi = {
                    val upiId = toMember?.upiId
                    if (upiId.isNullOrEmpty()) {
                        // In a real app we'd show a Snackbar here to ask the user to add a UPI ID
                        return@SettlementCard
                    }
                    val uri = UpiDeepLink.build(
                        receiverUpiId = upiId,
                        receiverName = toMember.name,
                        amount = txn.amount
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(Intent.createChooser(intent, "Pay via UPI"))
                },
                onMarkSettled = { onMarkSettled(txn) }
            )
        }
    }
}

@Composable
fun AlgorithmInfoCard(memberCount: Int, transactionCount: Int) {
    val naive = if (memberCount > 1) memberCount * (memberCount - 1) / 2 else 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Debt Minimization Active",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Reduced from $naive possible transactions → $transactionCount",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun SettlementCard(
    from: GroupMember?,
    to: GroupMember?,
    amount: Double,
    isSettled: Boolean,
    onPayUpi: () -> Unit,
    onMarkSettled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = to?.name?.take(1) ?: "?",
                    color = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${from?.name ?: "Unknown"} owes ${to?.name ?: "Unknown"}",
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "₹${amount.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (from?.name == "You") Color(0xFFEF4444) else Color(0xFF10B981)
                )
            }

            if (!isSettled) {
                if (from?.name == "You") {
                    Button(
                        onClick = onPayUpi,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Pay via UPI", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onMarkSettled,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Mark Settled")
                    }
                }
            } else {
                Text("Settled", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
    }
}
