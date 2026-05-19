package com.splitmate.android.data.repository

import com.splitmate.android.data.local.dao.ExpenseDao
import com.splitmate.android.data.local.dao.SettlementDao
import com.splitmate.android.data.local.entity.ExpenseEntity
import com.splitmate.android.data.local.entity.SettlementEntity
import com.splitmate.android.data.remote.ExpenseApi
import com.splitmate.android.data.remote.dto.CreateExpenseRequest
import com.splitmate.android.data.remote.dto.ExpenseUpdate
import com.splitmate.android.data.remote.dto.ExpenseResponse
import com.splitmate.android.data.remote.dto.MarkSettlementRequest
import com.splitmate.android.data.remote.dto.SettlementResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val api: ExpenseApi,
    private val expenseDao: ExpenseDao,
    private val settlementDao: SettlementDao
) {
    fun getExpenses(groupId: String): Flow<List<ExpenseEntity>> = flow {
        val cached = expenseDao.getExpenses(groupId).first()
        if (cached.isNotEmpty()) emit(cached)

        try {
            val remote = api.getExpenses(groupId)
            val entities = remote.map { it.toEntity() }
            expenseDao.insertAll(entities)
        } catch (e: Exception) {
            // Ignore offline network errors
        }

        emitAll(expenseDao.getExpenses(groupId))
    }

    suspend fun addExpense(groupId: String, request: CreateExpenseRequest) {
        // Optimistic UI updates could be added here, but for simplicity we await network
        val created = api.addExpense(groupId, request)
        expenseDao.insert(created.toEntity())
    }

    fun getSettlements(groupId: String): Flow<List<SettlementEntity>> = flow {
        val cached = settlementDao.getSettlements(groupId).first()
        if (cached.isNotEmpty()) emit(cached)

        try {
            refreshSettlements(groupId)
        } catch (e: Exception) {}

        emitAll(settlementDao.getSettlements(groupId))
    }

    suspend fun refreshSettlements(groupId: String) {
        val remote = api.getSettlements(groupId)
        val entities = remote.settlements.map { it.toEntity(remote.groupId) }
        settlementDao.insertAll(entities)
    }

    suspend fun handleRemoteUpdate(update: ExpenseUpdate) {
        update.expense?.let { remote ->
            expenseDao.insert(remote.toEntity())
        }

        update.settlements?.let { remoteSettlements ->
            val entities = remoteSettlements.map { it.toEntity(update.expense?.groupId.orEmpty()) }
            settlementDao.insertAll(entities)
        }
    }

    suspend fun markSettlementAsSettled(
        groupId: String,
        settlementId: String,
        fromUserId: String,
        toUserId: String,
        amount: Double
    ) {
        api.markSettlement(
            groupId,
            MarkSettlementRequest(
                fromUserId = fromUserId,
                toUserId = toUserId,
                amount = amount
            )
        )
        settlementDao.markAsSettled(settlementId)
        refreshSettlements(groupId)
    }

    private fun ExpenseResponse.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            groupId = groupId,
            description = description,
            amount = amount,
            paidBy = paidBy,
            paidByName = paidByName ?: paidBy.take(8),
            category = category,
            splitType = splitType,
            date = date ?: createdAt?.take(10).orEmpty()
        )
    }

    private fun SettlementResponse.toEntity(fallbackGroupId: String): SettlementEntity {
        val resolvedGroupId = groupId ?: fallbackGroupId
        val resolvedId = id ?: "$resolvedGroupId:$fromUserId:$toUserId:${amount}"
        return SettlementEntity(
            id = resolvedId,
            groupId = resolvedGroupId,
            fromUserId = fromUserId,
            fromUserName = fromUserName ?: fromUserId.take(8),
            toUserId = toUserId,
            toUserName = toUserName ?: toUserId.take(8),
            amount = amount,
            isSettled = isSettled ?: false
        )
    }
}
