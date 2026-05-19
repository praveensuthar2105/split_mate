package com.splitmate.android.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
fun PercentageSplitScreen(
    totalAmount: Double,
    members: List<GroupMember>,
    onConfirm: (Map<String, Double>) -> Unit
) {
    val percentages = remember {
        mutableStateMapOf<String, String>().apply {
            members.forEach { put(it.id, "") }
        }
    }

    val totalPercentage by derivedStateOf {
        percentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    }

    val isTotalCorrect = Math.abs(totalPercentage - 100.0) < 0.01

    val perPersonAmounts by derivedStateOf {
        members.associate { member ->
            val pct = percentages[member.id]?.toDoubleOrNull() ?: 0.0
            member.id to (totalAmount * pct / 100.0)
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { onConfirm(perPersonAmounts) },
                enabled = isTotalCorrect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTotalCorrect) Color(0xFF00D4AA) else Color.Gray
                )
            ) {
                Text(
                    text = if (isTotalCorrect) "Confirm Split" else "Total must be 100% (Current: ${"%.1f".format(totalPercentage)}%)",
                    color = Color.Black
                )
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
                        "Split by Percentage",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        "Total: ₹${"%.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            items(members) { member ->
                PercentageInputRow(
                    member = member,
                    percentage = percentages[member.id] ?: "",
                    amount = perPersonAmounts[member.id] ?: 0.0,
                    onPercentageChange = { percentages[member.id] = it }
                )
            }
        }
    }
}

@Composable
fun PercentageInputRow(
    member: GroupMember,
    percentage: String,
    amount: Double,
    onPercentageChange: (String) -> Unit
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
                Text("₹${"%.2f".format(amount)}", color = Color(0xFF00D4AA), fontSize = 14.sp)
            }

            OutlinedTextField(
                value = percentage,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onPercentageChange(it) },
                suffix = { Text("%", color = Color.White) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00D4AA),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}
