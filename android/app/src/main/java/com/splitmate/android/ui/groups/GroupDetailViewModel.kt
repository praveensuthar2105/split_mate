package com.splitmate.android.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.remote.WebSocketManager
import com.splitmate.android.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val webSocketManager: WebSocketManager,
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

    // Feed expenses from Room DB to UI
    val expenses = expenseRepository.getExpenses(groupId).map { entities ->
        entities.map { entity ->
            ExpenseUiModel(
                id = entity.id,
                description = entity.description,
                amount = entity.amount,
                paidBy = entity.paidByName,
                date = entity.date,
                categoryIcon = getIconForCategory(entity.category)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
}
