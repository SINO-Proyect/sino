package com.app.sino.data.repository

import com.app.sino.data.remote.UserApi
import com.app.sino.data.remote.dto.UserDto
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class UserRepository(
    private val api: UserApi
) {
    fun syncUser(user: UserDto): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        try {
            // First try to get by Firebase UID
            if (user.firebaseUid != null) {
                val response = api.getUserByFirebaseUid(user.firebaseUid)
                if (response.isSuccessful && response.body()?.data != null) {
                    emit(Resource.Success(response.body()!!.data!!))
                    return@flow
                }
            }

            // If not found, try by Email
            val emailResponse = api.getUserByEmail(user.email)
            if (emailResponse.isSuccessful && emailResponse.body()?.data != null) {
                var existingUser = emailResponse.body()!!.data!!
                var needsUpdate = false

                // If found by email, update UID if different
                if (user.firebaseUid != null && existingUser.firebaseUid != user.firebaseUid) {
                    existingUser = existingUser.copy(firebaseUid = user.firebaseUid)
                    needsUpdate = true
                }
                
                // Update Username if provided and different/missing
                if (user.username != null && existingUser.username != user.username) {
                    existingUser = existingUser.copy(username = user.username)
                    needsUpdate = true
                }

                // Update FullName if provided and different/missing
                if (user.fullName != null && existingUser.fullName != user.fullName) {
                    existingUser = existingUser.copy(fullName = user.fullName)
                    needsUpdate = true
                }

                if (needsUpdate) {
                     existingUser.idUser?.let { id ->
                         val updateResponse = api.updateUser(id, existingUser)
                         if (updateResponse.isSuccessful && updateResponse.body()?.data != null) {
                             emit(Resource.Success(updateResponse.body()!!.data!!))
                             return@flow
                         }
                     }
                }
                emit(Resource.Success(existingUser))
            } else {
                // Not found by UID or Email -> Create
                val createResponse = api.createUser(user)
                if (createResponse.isSuccessful && createResponse.body()?.data != null) {
                    emit(Resource.Success(createResponse.body()!!.data!!))
                } else {
                    emit(Resource.Error(createResponse.message() ?: "Failed to sync user"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    fun getUser(firebaseUid: String): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getUserByFirebaseUid(firebaseUid)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error fetching user"))
        }
    }

    fun checkUsername(username: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.checkUsername(username)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error("Error checking username"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error checking username"))
        }
    }
}
