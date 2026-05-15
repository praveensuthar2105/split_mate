package com.splitmate.android.data.remote.dto

// DTOs (Data Transfer Objects) are kept separate from Domain Models to isolate 
// the rest of the app from backend API changes. If the JSON structure changes, 
// we only update these files and their mappers.

data class SendOtpRequest(
    val phone: String
)

data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)

data class TokenResponse(
    val token: String,
    val userId: String
)

data class ProfileRequest(
    val name: String,
    val upiId: String?
)

data class UserResponse(
    val id: String,
    val phone: String,
    val name: String?,
    val avatarUrl: String?,
    val upiId: String?,
    val upiIdVerified: Boolean
)
