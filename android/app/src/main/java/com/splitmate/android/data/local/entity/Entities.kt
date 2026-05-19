package com.splitmate.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val photoUrl: String?,
    val createdBy: String,
    val groupType: String,
    val isArchived: Boolean,
    val budgetAmount: Double?,
    val lastActivity: String,
    val balance: Double // Positive = owed, Negative = owe
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey val id: String, // format: "groupId_userId"
    val groupId: String,
    val userId: String,
    val role: String,
    val name: String,
    val upiId: String?
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val paidByName: String,
    val category: String,
    val splitType: String,
    val date: String
)

@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double,
    val isSettled: Boolean
)
