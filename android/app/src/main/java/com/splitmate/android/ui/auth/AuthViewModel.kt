package com.splitmate.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitmate.android.data.remote.AuthApi
import com.splitmate.android.data.remote.dto.SendOtpRequest
import com.splitmate.android.data.remote.dto.VerifyOtpRequest
import com.splitmate.android.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val otpSent: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

/**
 * Manages the UI state for the Login screen.
 * Annotated with @HiltViewModel so Hilt can automatically inject dependencies
 * (AuthApi and TokenManager) without manual factory boilerplate.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Using MutableStateFlow to manage view state. This ensures the UI always
    // observes the latest state, even across configuration changes (e.g. rotation).
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Calls the backend to send an OTP to the provided phone number.
     */
    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.sendOtp(SendOtpRequest(phone))
                if (response.isSuccessful) {
                    // Update state to show the OTP input field
                    _uiState.update { it.copy(isLoading = false, otpSent = true) }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to send OTP. Please try again.") 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error. Check your connection.") 
                }
            }
        }
    }

    /**
     * Verifies the entered OTP against the backend.
     * On success, saves the JWT to DataStore via TokenManager.
     */
    fun verifyOtp(phone: String, otp: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.verifyOtp(VerifyOtpRequest(phone, otp))
                if (response.isSuccessful && response.body() != null) {
                    // Extract token and save to persistent storage
                    val body = response.body()!!
                    val token = body.token
                    tokenManager.saveToken(token)
                    tokenManager.saveUserId(body.userId)
                    
                    // Trigger UI navigation by updating state
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Invalid OTP. Please check and try again.") 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error. Check your connection.") 
                }
            }
        }
    }

    /**
     * Clears transient UI errors (like incorrect OTP messages) so they don't persist
     * when the user tries typing again.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
