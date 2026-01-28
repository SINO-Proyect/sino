package com.app.sino.data.remote

import com.app.sino.data.remote.dto.ApiResponse
import com.app.sino.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("api/users")
    suspend fun createUser(@Body user: UserDto): Response<ApiResponse<UserDto>>

    @GET("api/users/firebase/{uid}")
    suspend fun getUserByFirebaseUid(@Path("uid") uid: String): Response<ApiResponse<UserDto>>

    @GET("api/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<ApiResponse<UserDto>>

    @GET("api/users/check-username/{username}")
    suspend fun checkUsername(@Path("username") username: String): Response<ApiResponse<Boolean>>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserDto): Response<ApiResponse<UserDto>>

    @POST("api/users/{id}/last-login")
    suspend fun updateLastLogin(@Path("id") id: Int): Response<ApiResponse<Unit>>
}
