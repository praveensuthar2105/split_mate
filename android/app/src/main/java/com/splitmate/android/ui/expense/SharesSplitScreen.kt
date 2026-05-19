package com.splitmate.android.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitmate.android.ui.settle.GroupMember

@Composable
fun SharesSplitScreen(
    totalAmount: Double,
    members: List<GroupMember>,
    onConfirm: (Map<String, Double>) -> Unit
) {
    val shares = remember {
        mutableStateMapOf<String, Int>().apply {
            members.forEach { put(it.id, 1) }
        }
    }

    val totalShares by derivedStateOf {
        shares.values.sum().coerceAtLeast(1)
    }

    val perPersonAmounts by derivedStateOf {
        members.associate { member ->
            val shareCount = shares[member.id] ?: 0
            member.id to (totalAmount * shareCount / totalShares)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { onConfirm(perPersonAmounts) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("Confirm Split", color = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(padding)
        ) {
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Split by Shares",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        "Total: ₹${"%.2f".format(totalAmount)} ($totalShares shares)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            items(members) { member ->
                ShareInputRow(
                    member = member,
                    shareCount = shares[member.id] ?: 0,
                    amount = perPersonAmounts[member.id] ?: 0.0,
                    onShareChange = { shares[member.id] = it }
                )
            }
        }
    }
}

@Composable
fun ShareInputRow(
    member: GroupMember,
    shareCount: Int,
    amount: Double,
    onShareChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold, color = Color.White)
                Text("₹${"%.2f".format(amount)}", color = Color(0xFF10B981), fontSize = 14.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (shareCount > 0) onShareChange(shareCount - 1) },
                    enabled = shareCount > 0
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                }

                Text(
                    text = shareCount.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = { onShareChange(shareCount + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                }
            }
        }
    }
}
