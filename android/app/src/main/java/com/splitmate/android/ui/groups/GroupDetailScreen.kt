package com.splitmate.android.ui.groups

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.splitmate.android.ui.analytics.AnalyticsScreen
import com.splitmate.android.domain.model.GroupAnalytics
import com.splitmate.android.ui.expense.AddExpenseSheet
import com.splitmate.android.ui.settle.SettleUpScreen
import com.splitmate.android.ui.groups.BudgetProgressBar
import com.splitmate.android.domain.model.GroupBudget

data class ExpenseUiModel(
    val id: String, val description: String, val amount: Double,
    val paidBy: String, val date: String, val category: String, val categoryIcon: String
)

@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddExpenseClick: (amount: Double, description: String, splitType: String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    val tabs = listOf("Expenses", "Settle Up", "Analytics", "Activity")
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val group by viewModel.group.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val settlements by viewModel.settlements.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val inviteLink by viewModel.inviteLink.collectAsStateWithLifecycle()

    LaunchedEffect(uiMessage) {
        val message = uiMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    LaunchedEffect(inviteLink) {
        val link = inviteLink ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        context.startActivity(Intent.createChooser(intent, "Share SplitMate invite"))
        viewModel.clearInviteLink()
    }

    // Real values would come from a Group query in the ViewModel
    val groupName = group?.name ?: "Group"
    val budgetTotal = group?.budgetAmount ?: 0.0
    val budgetSpent = expenses.sumOf { it.amount }
    val budgetPercent = if (budgetTotal > 0) (budgetSpent / budgetTotal).toFloat().coerceIn(0f, 1f) else 0f

    val myBalance = group?.balance ?: 0.0

    val analytics = GroupAnalytics(
        groupId = groupId,
        monthlyTotals = mapOf(
            "Current" to budgetSpent
        ),
        categoryBreakdown = expenses
            .groupBy { it.categoryIcon }
            .mapValues { (_, categoryExpenses) -> categoryExpenses.sumOf { it.amount } },
        totalSpend = budgetSpent
    )

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showAddExpenseSheet = true },
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
                GroupTopBar(groupName, onBackClick, onInviteClick = viewModel::generateInviteLink)
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
                        settlements = settlements,
                        members = members,
                        onMarkSettled = viewModel::markSettlementAsSettled
                    )
                    2 -> AnalyticsScreen(analytics = analytics)
                    3 -> CenterTextContent("Activity log coming soon")
                }
            }
        }
    }

    if (showAddExpenseSheet) {
        AddExpenseSheet(
            onDismiss = { showAddExpenseSheet = false },
            onSaveClick = { amount, description, splitType ->
                showAddExpenseSheet = false
                if (splitType != "Equal") {
                    onAddExpenseClick(amount, description, splitType)
                } else {
                    viewModel.addExpense(amount, description, splitType)
                }
            },
            onOcrClick = {
                showAddExpenseSheet = false
                // For OCR, we might need a different flow later, but for now we navigate
                onAddExpenseClick(0.0, "", "Itemized")
            },
            onVoiceClick = {
                showAddExpenseSheet = false
                onAddExpenseClick(0.0, "", "Equal")
            }
        )
    }
}

@Composable
private fun GroupTopBar(groupName: String, onBackClick: () -> Unit, onInviteClick: () -> Unit) {
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
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onInviteClick) {
            Icon(Icons.Default.Share, contentDescription = "Share invite", tint = Color.White)
        }
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

            Spacer(Modifier.height(12.dp))

            // Use the new BudgetProgressBar we just created
            BudgetProgressBar(
                budget = GroupBudget(groupId = "", totalAmount = total),
                totalSpend = spent
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
            Spacer(Modifier.height(6.dp))
            AssistChip(
                onClick = { },
                label = { Text(expense.category.ifBlank { "Other" }, fontSize = 11.sp) },
                leadingIcon = { Text(expense.categoryIcon, fontSize = 13.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFFCBD5E1)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            )
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
