package com.splitmate.android.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    groupRepository: GroupRepository
) : ViewModel() {

    // Transforms Room DB Entities directly into UI Models for the Compose screen
    val groups = groupRepository.getGroups().map { entities ->
        entities.map { entity ->
            GroupUiModel(
                id = entity.id,
                name = entity.name,
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
}
