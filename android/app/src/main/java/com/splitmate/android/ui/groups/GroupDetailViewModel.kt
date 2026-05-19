package com.splitmate.android.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.remote.WebSocketManager
import com.splitmate.android.data.remote.dto.CreateExpenseRequest
import com.splitmate.android.data.repository.ExpenseRepository
import com.splitmate.android.data.repository.GroupRepository
import com.splitmate.android.ui.settle.GroupMember
import com.splitmate.android.ui.settle.SettlementTransaction
import com.splitmate.android.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val webSocketManager: WebSocketManager,
    private val tokenManager: TokenManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve the groupId passed via Jetpack Navigation route arguments
    val groupId: String = checkNotNull(savedStateHandle["groupId"])

    init {
        webSocketManager.connect(groupId)

        viewModelScope.launch {
            webSocketManager.expenseUpdates.collect { update ->
                expenseRepository.handleRemoteUpdate(update)
            }
        }
    }

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()
    private val _inviteLink = MutableStateFlow<String?>(null)
    val inviteLink: StateFlow<String?> = _inviteLink.asStateFlow()

    val group = groupRepository.getGroup(groupId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Feed expenses from Room DB to UI
    val expenses = expenseRepository.getExpenses(groupId).map { entities ->
        entities.map { entity ->
            ExpenseUiModel(
                id = entity.id,
                description = entity.description,
                amount = entity.amount,
                paidBy = entity.paidByName,
                date = entity.date,
                category = entity.category,
                categoryIcon = getIconForCategory(entity.category)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val members = groupRepository.getGroupMembers(groupId).map { entities ->
        entities.map { entity ->
            GroupMember(
                id = entity.userId,
                name = entity.name.ifBlank { entity.userId.take(8) },
                upiId = entity.upiId
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settlements = expenseRepository.getSettlements(groupId).map { entities ->
        entities.map { entity ->
            SettlementTransaction(
                id = entity.id,
                fromUserId = entity.fromUserId,
                toUserId = entity.toUserId,
                amount = entity.amount,
                isSettled = entity.isSettled
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addExpense(
        amount: Double,
        description: String,
        splitType: String,
        customSplits: Map<String, Double>? = null
    ) {
        viewModelScope.launch {
            val currentUserId = tokenManager.userIdFlow.first()
            if (currentUserId.isNullOrBlank()) {
                _uiMessage.value = "Please log in again before adding expenses."
                return@launch
            }

            val participantIds = members.value.map { it.id }
            if (participantIds.isEmpty()) {
                _uiMessage.value = "Group must have at least one member."
                return@launch
            }

            try {
                expenseRepository.addExpense(
                    groupId,
                    CreateExpenseRequest(
                        description = description,
                        amount = amount,
                        paidBy = currentUserId,
                        category = "",
                        splitType = splitType.uppercase(),
                        participantIds = participantIds,
                        customSplits = customSplits
                    )
                )
                expenseRepository.refreshSettlements(groupId)
                _uiMessage.value = "Expense added."
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: "Could not add expense."
            }
        }
    }

    fun markSettlementAsSettled(settlement: SettlementTransaction) {
        viewModelScope.launch {
            try {
                expenseRepository.markSettlementAsSettled(
                    groupId = groupId,
                    settlementId = settlement.id,
                    fromUserId = settlement.fromUserId,
                    toUserId = settlement.toUserId,
                    amount = settlement.amount
                )
                _uiMessage.value = "Settlement marked."
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: "Could not mark settlement."
            }
        }
    }

    fun generateInviteLink() {
        viewModelScope.launch {
            try {
                _inviteLink.value = groupRepository.generateInviteLink(groupId)
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: "Could not generate invite link."
            }
        }
    }

    fun clearInviteLink() {
        _inviteLink.update { null }
    }

    fun clearMessage() {
        _uiMessage.update { null }
    }

    // Helper to turn string categories into Emojis for the UI
    private fun getIconForCategory(category: String): String {
        return when (category.lowercase()) {
            "food" -> "🍔"
            "transport" -> "🚕"
            "rent", "housing" -> "🏠"
            "groceries" -> "🛒"
            "entertainment" -> "🍿"
            "utilities" -> "⚡"
            else -> "💸"
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}
