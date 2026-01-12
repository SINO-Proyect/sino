package com.app.sino.data.remote

import com.app.sino.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("recover")
    suspend fun recoverPassword(@Body request: RecoverRequest): Response<AuthResponse>

    @POST("refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("send-verification")
    suspend fun sendVerificationEmail(@Body request: VerificationRequest): Response<AuthResponse>

    @GET("verify")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<AuthResponse>

    @POST("logout")
    suspend fun logout(@Header("Authorization") token: String): Response<AuthResponse>
}
