package com.app.sino.data.repository

import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.AuthApi
import com.app.sino.data.remote.dto.*
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) {

    fun register(request: RegisterRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    body.data.idToken?.let { tokenManager.saveToken(it) }
                    body.data.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                    body.data.email?.let { tokenManager.saveEmail(it) }
                    body.data.emailVerified?.let { tokenManager.saveEmailVerified(it) }
                }
                emit(Resource.Success(body))
            } else {
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun login(request: LoginRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    body.data.idToken?.let { tokenManager.saveToken(it) }
                    body.data.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                    body.data.email?.let { tokenManager.saveEmail(it) }
                    body.data.emailVerified?.let { tokenManager.saveEmailVerified(it) }
                }
                emit(Resource.Success(body))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun recoverPassword(email: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.recoverPassword(RecoverRequest(email))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Recovery failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error"))
        }
    }

    fun logout(): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        val token = tokenManager.getToken()
        if (token != null) {
            try {
                api.logout("Bearer $token")
            } catch (e: Exception) {

            }
        }
        tokenManager.clearTokens()
        emit(Resource.Success(AuthResponse(true, "Logged out")))
    }

    fun checkAuth(): Flow<Boolean> = flow {
        val token = tokenManager.getToken()
        if (token != null) {
            try {
                val response = api.verifyToken("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    data?.emailVerified?.let { tokenManager.saveEmailVerified(it) }
                    data?.email?.let { tokenManager.saveEmail(it) }
                    emit(true)
                } else {
                    // If the account was deleted or token expired (401, 404)
                    if (response.code() == 401 || response.code() == 404) {
                        tokenManager.clearTokens()
                        emit(false)
                    } else {
                        // For other errors (500, etc), we might want to allow offline access
                        emit(true)
                    }
                }
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 404) {
                    tokenManager.clearTokens()
                    emit(false)
                } else {
                    // For robustness, if it's not 401/404, we assume offline/temp issue
                    emit(true)
                }
            } catch (e: Exception) {
                // Network error or other issues, allow offline access
                emit(true)
            }
        } else {
            emit(false)
        }
    }

    fun getEmail(): String? {
        return tokenManager.getEmail()
    }

    fun isEmailVerified(): Boolean {
        return tokenManager.isEmailVerified()
    }

    fun sendVerificationEmail(): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        val token = tokenManager.getToken()
        if (token == null) {
            emit(Resource.Error("Not logged in"))
            return@flow
        }
        try {
            val response = api.sendVerificationEmail(VerificationRequest(token))
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to send email"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error"))
        }
    }
}
