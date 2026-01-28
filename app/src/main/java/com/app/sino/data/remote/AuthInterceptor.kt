package com.app.sino.data.remote

import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.dto.AuthResponse
import com.app.sino.data.remote.dto.RefreshTokenRequest
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    private val gson = Gson()
    private val refreshClient = OkHttpClient()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Skip auth for public endpoints
        if (path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/refresh-token") ||
            path.contains("/auth/recover")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.getToken()
        val requestWithToken = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithToken)

        if (response.code == 401) {
            response.close() // Close the body of the failed response

            synchronized(this) {
                // Check if token updated while we waited
                val currentToken = tokenManager.getToken()
                if (currentToken != null && currentToken != token) {
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(newRequest)
                }

                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken != null) {
                    if (refreshAccessToken(refreshToken)) {
                        val newToken = tokenManager.getToken()
                        if (newToken != null) {
                            val newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                            return chain.proceed(newRequest)
                        }
                    } else {
                        tokenManager.clearTokens()
                    }
                }
            }
        }

        return response
    }

    private fun refreshAccessToken(refreshToken: String): Boolean {
        return try {
            // Using the same base URL logic as RetrofitClient
            val url = "http://192.168.0.150:8080/auth/refresh-token"
            val requestBody = RefreshTokenRequest(refreshToken)
            val jsonBody = gson.toJson(requestBody)
            
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = refreshClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val authResponse = gson.fromJson(response.body!!.string(), AuthResponse::class.java)
                if (authResponse.success && authResponse.data != null) {
                    val data = authResponse.data
                    if (data.idToken != null) tokenManager.saveToken(data.idToken)
                    if (data.refreshToken != null) tokenManager.saveRefreshToken(data.refreshToken)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
