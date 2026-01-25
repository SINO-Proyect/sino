package com.app.sino.data.remote

import com.app.sino.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/recover")
    suspend fun recoverPassword(@Body request: RecoverRequest): Response<AuthResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/send-verification")
    suspend fun sendVerificationEmail(@Body request: VerificationRequest): Response<AuthResponse>

    @GET("auth/verify")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<AuthResponse>
}