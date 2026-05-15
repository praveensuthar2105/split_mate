package com.splitmate.android.data.repository

import com.splitmate.android.data.local.dao.ExpenseDao
import com.splitmate.android.data.local.dao.SettlementDao
import com.splitmate.android.data.local.entity.ExpenseEntity
import com.splitmate.android.data.local.entity.SettlementEntity
import com.splitmate.android.data.remote.ExpenseApi
import com.splitmate.android.data.remote.dto.CreateExpenseRequest
import com.splitmate.android.data.remote.dto.ExpenseUpdate
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
            val entities = remote.map {
                ExpenseEntity(
                    id = it.id, groupId = it.groupId, description = it.description,
                    amount = it.amount, paidBy = it.paidBy, paidByName = it.paidByName,
                    category = it.category, splitType = it.splitType, date = it.date
                )
            }
            expenseDao.insertAll(entities)
        } catch (e: Exception) {
            // Ignore offline network errors
        }

        emitAll(expenseDao.getExpenses(groupId))
    }

    suspend fun addExpense(groupId: String, request: CreateExpenseRequest) {
        // Optimistic UI updates could be added here, but for simplicity we await network
        val created = api.addExpense(groupId, request)
        expenseDao.insert(
            ExpenseEntity(
                id = created.id, groupId = created.groupId, description = created.description,
                amount = created.amount, paidBy = created.paidBy, paidByName = created.paidByName,
                category = created.category, splitType = created.splitType, date = created.date
            )
        )
    }

    fun getSettlements(groupId: String): Flow<List<SettlementEntity>> = flow {
        val cached = settlementDao.getSettlements(groupId).first()
        if (cached.isNotEmpty()) emit(cached)

        try {
            val remote = api.getSettlements(groupId)
            val entities = remote.map {
                SettlementEntity(
                    id = it.id, groupId = it.groupId, fromUserId = it.fromUserId,
                    fromUserName = it.fromUserName, toUserId = it.toUserId,
                    toUserName = it.toUserName, amount = it.amount, isSettled = it.isSettled
                )
            }
            settlementDao.insertAll(entities)
        } catch (e: Exception) {}

        emitAll(settlementDao.getSettlements(groupId))
    }

    suspend fun handleRemoteUpdate(update: ExpenseUpdate) {
        update.expense?.let { remote ->
            expenseDao.insert(
                ExpenseEntity(
                    id = remote.id, groupId = remote.groupId, description = remote.description,
                    amount = remote.amount, paidBy = remote.paidBy, paidByName = remote.paidByName,
                    category = remote.category, splitType = remote.splitType, date = remote.date
                )
            )
        }

        update.settlements?.let { remoteSettlements ->
            val entities = remoteSettlements.map {
                SettlementEntity(
                    id = it.id, groupId = it.groupId, fromUserId = it.fromUserId,
                    fromUserName = it.fromUserName, toUserId = it.toUserId,
                    toUserName = it.toUserName, amount = it.amount, isSettled = it.isSettled
                )
            }
            settlementDao.insertAll(entities)
        }
    }
}
