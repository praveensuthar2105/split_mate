package com.splitmate.android.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Transforms Room DB Entities directly into UI Models for the Compose screen
    val groups = groupRepository.getGroups().map { entities ->
        entities.map { entity ->
            GroupUiModel(
                id = entity.id,
                name = entity.name,
                groupType = entity.groupType,
                lastActivity = entity.lastActivity,
                balance = entity.balance,
                isArchived = entity.isArchived
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList() // The UI will start with an empty list while waiting for Room
    )

    fun joinGroup(inviteText: String) {
        if (inviteText.isBlank()) {
            _uiMessage.value = "Paste an invite link or group id."
            return
        }

        viewModelScope.launch {
            try {
                groupRepository.joinGroup(inviteText)
                _uiMessage.value = "Joined group."
            } catch (e: Exception) {
                _uiMessage.value = e.message ?: "Could not join group."
            }
        }
    }

    fun clearMessage() {
        _uiMessage.update { null }
    }
}
