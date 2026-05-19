package com.splitmate.android.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdGroupId: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun createGroup(name: String, groupType: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Group name is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val created = groupRepository.createGroup(
                    name = name.trim(),
                    groupType = groupType.lowercase()
                )
                _uiState.update {
                    it.copy(isLoading = false, createdGroupId = created.id)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Could not create group.")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
