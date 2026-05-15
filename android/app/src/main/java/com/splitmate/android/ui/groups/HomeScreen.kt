package com.splitmate.android.ui.groups

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class GroupUiModel(
    val id: String,
    val name: String,
    val lastActivity: String,
    val balance: Double,
    val isArchived: Boolean = false
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onGroupClick: (String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Observe database-backed groups from the ViewModel
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    // Use mock data for testing UI if database is empty
    val displayGroups = if (groups.isNotEmpty()) groups else listOf(
        GroupUiModel("1", "Goa Trip 2026", "Rohan added Dinner at Curlies", 1250.0),
        GroupUiModel("2", "Flat 402", "You paid for Electricity Bill", -450.0),
        GroupUiModel("3", "Office Lunch Squad", "Arjun settled up", 0.0)
    )

    // Calculate balance based on displayGroups so mock data is reflected
    val totalBalance = displayGroups.sumOf { it.balance }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClick,
                containerColor = Color(0xFF10B981),
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF020617)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeTopBar(totalBalance = totalBalance, onProfileClick = onProfileClick)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Your Groups",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (displayGroups.isEmpty()) {
                        item {
                            Text(
                                "No groups yet. Tap + to create one!",
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                    }

                    items(displayGroups) { group ->
                        GroupCard(group = group, onClick = { onGroupClick(group.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(totalBalance: Double, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFF94A3B8))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total Balance",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            val balanceColor = when {
                totalBalance > 0 -> Color(0xFF10B981)
                totalBalance < 0 -> Color(0xFFEF4444)
                else -> Color.White
            }
            val balancePrefix = if (totalBalance > 0) "You are owed " else if (totalBalance < 0) "You owe " else "All settled up"
            val formattedAmount = if (totalBalance != 0.0) "₹${Math.abs(totalBalance).toInt()}" else ""

            Text(
                text = "$balancePrefix$formattedAmount",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = balanceColor
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFF94A3B8))
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            )
        }
    }
}

@Composable
private fun GroupCard(group: GroupUiModel, onClick: () -> Unit) {
    val balanceColor = when {
        group.balance > 0 -> Color(0xFF10B981)
        group.balance < 0 -> Color(0xFFEF4444)
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B).copy(alpha = if (group.isArchived) 0.5f else 0.8f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.lastActivity,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                if (group.balance != 0.0) {
                    Text(
                        text = if (group.balance > 0) "Owes you" else "You owe",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "₹${Math.abs(group.balance).toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                } else {
                    Text(
                        text = "Settled",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = balanceColor
                    )
                }
            }
        }
    }
}
