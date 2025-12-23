package com.app.sino.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("phoneNumber") val phoneNumber: String? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RecoverRequest(
    @SerializedName("email") val email: String
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthData? = null
)

data class AuthData(
    @SerializedName("uid") val uid: String?,
    @SerializedName("idToken") val idToken: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("expiresIn") val expiresIn: String?,
    @SerializedName("localId") val localId: String?
)
