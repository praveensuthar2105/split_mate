package com.splitmate.android.data.remote

import com.splitmate.android.data.remote.dto.ProfileRequest
import com.splitmate.android.data.remote.dto.SendOtpRequest
import com.splitmate.android.data.remote.dto.TokenResponse
import com.splitmate.android.data.remote.dto.UserResponse
import com.splitmate.android.data.remote.dto.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Retrofit interface defining the authentication contract with the backend.
 * All functions are suspend functions, meaning Retrofit will execute them asynchronously 
 * on a background thread automatically without blocking the UI.
 */
interface AuthApi {
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<Unit>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<TokenResponse>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: ProfileRequest): Response<UserResponse>
}
