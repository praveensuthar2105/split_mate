package com.splitmate.android.ui.expense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splitmate.android.domain.model.LineItem
import com.splitmate.android.ui.settle.GroupMember

@Composable
fun ItemizedSplitScreen(
    lineItems: List<LineItem>,
    members: List<GroupMember>,
    onConfirm: (Map<String, Double>) -> Unit  // userId → their total
) {
    // Track which member claimed each item
    val claims = remember {
        mutableStateMapOf<Int, String>()  // itemIndex → userId
    }

    val perPersonTotals by derivedStateOf {
        members.associate { member ->
            member.id to lineItems
                .filterIndexed { i, _ -> claims[i] == member.id }
                .sumOf { it.amount }
        }
    }

    val allClaimed by derivedStateOf {
        lineItems.indices.all { i -> claims.containsKey(i) }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { onConfirm(perPersonTotals) },
                enabled = allClaimed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4AA))
            ) {
                Text(
                    text = if (allClaimed) "Confirm Split" else "Assign all items to continue",
                    color = Color.Black
                )
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Assign Items",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            itemsIndexed(lineItems) { index, item ->
                ItemClaimCard(
                    item = item,
                    members = members,
                    claimedBy = claims[index],
                    onClaim = { memberId -> claims[index] = memberId }
                )
            }

            item {
                // Per-person summary
                Spacer(Modifier.height(24.dp))
                Text(
                    "Summary",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))

                members.forEach { member ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(member.name)
                        Text(
                            text = "₹${perPersonTotals[member.id]?.let { "%.2f".format(it) } ?: "0.00"}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00D4AA)
                        )
                    }
                }
                Spacer(Modifier.height(80.dp)) // Extra padding for bottom bar
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemClaimCard(
    item: LineItem,
    members: List<GroupMember>,
    claimedBy: String?,
    onClaim: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = if (claimedBy != null) BorderStroke(1.dp, Color(0xFF00D4AA).copy(alpha = 0.5f)) else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "₹${"%.2f".format(item.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members) { member ->
                    val isClaimed = claimedBy == member.id
                    FilterChip(
                        selected = isClaimed,
                        onClick = { onClaim(member.id) },
                        label = { Text(member.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00D4AA).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF00D4AA)
                        )
                    )
                }
            }
        }
    }
}
