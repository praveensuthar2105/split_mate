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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.splitmate.android.ui.settle.GroupMember
import com.splitmate.android.ui.settle.SettleUpScreen
import com.splitmate.android.ui.settle.SettlementTransaction

data class ExpenseUiModel(
    val id: String, val description: String, val amount: Double,
    val paidBy: String, val date: String, val categoryIcon: String
)

@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expenses", "Settle Up", "Analytics", "Activity")

    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    // Real values would come from a Group query in the ViewModel
    val groupName = "Goa Trip 2026"
    val budgetTotal = 15000.0
    val budgetSpent = expenses.sumOf { it.amount }
    val budgetPercent = if (budgetTotal > 0) (budgetSpent / budgetTotal).toFloat().coerceIn(0f, 1f) else 0f

    val myBalance = 1250.0

    // Mock Data for Settle Up
    val mockMembers = listOf(
        GroupMember("1", "You", "praveen@okaxis"),
        GroupMember("2", "Priya", "priya@okicici"),
        GroupMember("3", "Rohan", null)
    )
    val mockSettlements = listOf(
        SettlementTransaction("s1", "1", "2", 450.0, false),
        SettlementTransaction("s2", "3", "1", 120.0, false)
    )

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = onAddExpenseClick,
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))))
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GroupTopBar(groupName, onBackClick)
                GroupHeaderCard(myBalance, budgetSpent, budgetTotal, budgetPercent)

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF10B981)
                        )
                    },
                    divider = { Divider(color = Color.White.copy(alpha = 0.1f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    color = if (selectedTabIndex == index) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> ExpensesListContent(expenses)
                    1 -> SettleUpScreen(
                        settlements = mockSettlements,
                        members = mockMembers,
                        onMarkSettled = { /* TODO */ }
                    )
                    2 -> CenterTextContent("Analytics coming soon")
                    3 -> CenterTextContent("Activity log coming soon")
                }
            }
        }
    }
}

@Composable
private fun GroupTopBar(groupName: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 8.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF334155)),
            contentAlignment = Alignment.Center
        ) {
            Text(groupName.take(1), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.width(12.dp))
        Text(
            text = groupName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun GroupHeaderCard(balance: Double, spent: Double, total: Double, percent: Float) {
    val balanceColor = if (balance > 0) Color(0xFF10B981) else Color(0xFFEF4444)
    val balancePrefix = if (balance > 0) "You are owed" else "You owe"

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(balancePrefix, fontSize = 13.sp, color = Color(0xFF94A3B8))
            Text("₹${Math.abs(balance).toInt()}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = balanceColor)

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Group Budget", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Text("₹${spent.toInt()} / ₹${total.toInt()}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))

            val barColor = if (percent > 0.9f) Color(0xFFEF4444) else if (percent > 0.7f) Color(0xFFF59E0B) else Color(0xFF10B981)
            LinearProgressIndicator(
                progress = percent,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun ExpensesListContent(expenses: List<ExpenseUiModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        if (expenses.isEmpty()) {
            item {
                Text(
                    "No expenses yet. Add one below!",
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(expenses) { expense ->
            ExpenseListItem(expense)
            Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun ExpenseListItem(expense: ExpenseUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF334155).copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(expense.categoryIcon, fontSize = 20.sp)
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(expense.description, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.height(2.dp))
            Text("${expense.paidBy} paid • ${expense.date}", fontSize = 12.sp, color = Color(0xFF94A3B8))
        }

        Text("₹${expense.amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun CenterTextContent(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color(0xFF94A3B8))
    }
}
