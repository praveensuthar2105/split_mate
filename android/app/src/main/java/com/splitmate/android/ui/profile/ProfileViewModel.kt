package com.splitmate.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.remote.AuthApi
import com.splitmate.android.data.remote.dto.ProfileRequest
import com.splitmate.android.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val id: String = "",
    val phone: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val upiId: String? = null,
    val upiIdVerified: Boolean = false
)

data class ProfileUiState(
    val user: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val response = authApi.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    val userResponse = response.body()!!
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            user = UserProfile(
                                id = userResponse.id ?: "",
                                phone = userResponse.phone ?: "",
                                name = userResponse.name ?: "",
                                avatarUrl = userResponse.avatarUrl,
                                upiId = userResponse.upiId,
                                upiIdVerified = userResponse.upiIdVerified ?: false
                            )
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load profile details from server") 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.localizedMessage ?: "Failed to load profile") 
                }
            }
        }
    }

    fun updateUpiId(newUpiId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, error = null) }
                
                val currentUser = _uiState.value.user
                val profileRequest = ProfileRequest(
                    name = currentUser.name,
                    upiId = newUpiId
                )
                
                val response = authApi.updateProfile(profileRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            user = UserProfile(
                                id = updatedUser.id,
                                phone = updatedUser.phone,
                                name = updatedUser.name ?: "",
                                avatarUrl = updatedUser.avatarUrl,
                                upiId = updatedUser.upiId,
                                upiIdVerified = updatedUser.upiIdVerified
                            )
                        )
                    }
                    // Clear success message after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(saveSuccess = false) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to update UPI ID") 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = e.message ?: "Error updating profile") 
                }
            }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, error = null) }
                
                val currentUser = _uiState.value.user
                val profileRequest = ProfileRequest(
                    name = newName,
                    upiId = currentUser.upiId
                )
                
                val response = authApi.updateProfile(profileRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val updatedUser = response.body()!!
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            user = UserProfile(
                                id = updatedUser.id,
                                phone = updatedUser.phone,
                                name = updatedUser.name ?: "",
                                avatarUrl = updatedUser.avatarUrl,
                                upiId = updatedUser.upiId,
                                upiIdVerified = updatedUser.upiIdVerified
                            )
                        )
                    }
                    // Clear success message after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(saveSuccess = false) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to update name") 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = e.message ?: "Error updating profile") 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
