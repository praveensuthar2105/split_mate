package com.splitmate.android.data.remote.dto

// Group DTOs
data class GroupResponse(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val createdBy: String,
    val isArchived: Boolean,
    val budgetAmount: Double?,
    val lastActivity: String,
    val balance: Double
)

data class GroupMemberResponse(
    val groupId: String,
    val userId: String,
    val role: String,
    val name: String,
    val upiId: String?
)

data class GroupDetailResponse(
    val group: GroupResponse,
    val members: List<GroupMemberResponse>
)

// Expense DTOs
data class ExpenseResponse(
    val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val paidByName: String,
    val category: String,
    val splitType: String,
    val date: String
)

data class CreateExpenseRequest(
    val description: String,
    val amount: Double,
    val paidBy: String,
    val category: String,
    val splitType: String,
    val participants: List<String>
)

// Settlement DTOs
data class SettlementResponse(
    val id: String,
    val groupId: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double,
    val isSettled: Boolean
)

data class ExpenseUpdate(
    val type: String,
    val expense: ExpenseResponse?,
    val settlements: List<SettlementResponse>?
)
