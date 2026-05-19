package com.splitmate.android.data.remote.dto

import com.google.gson.annotations.SerializedName

// Group DTOs
data class GroupResponse(
    val id: String,
    val name: String,
    val photoUrl: String?,
    val createdBy: String,
    val groupType: String?,
    @SerializedName("archived")
    val isArchived: Boolean = false,
    val budgetAmount: Double?,
    val lastActivity: String?,
    val balance: Double?
)

data class GroupMemberResponse(
    val id: String?,
    val groupId: String,
    val userId: String,
    val role: String,
    val name: String?,
    val upiId: String?
)

data class GroupDetailResponse(
    val group: GroupResponse,
    val members: List<GroupMemberResponse>
)

data class CreateGroupRequest(
    val name: String,
    val groupType: String
)

data class InviteLinkResponse(
    val inviteLink: String
)

// Expense DTOs
data class ExpenseResponse(
    val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val paidByName: String?,
    val category: String,
    val splitType: String,
    val date: String?,
    val createdAt: String?
)

data class CreateExpenseRequest(
    val description: String,
    val amount: Double,
    val paidBy: String,
    val category: String,
    val splitType: String,
    val participantIds: List<String>,
    val customSplits: Map<String, Double>? = null
)

// Settlement DTOs
data class SettlementResponse(
    val id: String?,
    val groupId: String?,
    val fromUserId: String,
    val fromUserName: String?,
    val toUserId: String,
    val toUserName: String?,
    val amount: Double,
    @SerializedName("settled")
    val isSettled: Boolean?
)

data class MarkSettlementRequest(
    val fromUserId: String,
    val toUserId: String,
    val amount: Double
)

data class SettlementSummaryResponse(
    val groupId: String,
    val balances: Map<String, Double>,
    val settlements: List<SettlementResponse>
)

data class ExpenseUpdate(
    val type: String,
    val expense: ExpenseResponse?,
    val settlements: List<SettlementResponse>?
)
