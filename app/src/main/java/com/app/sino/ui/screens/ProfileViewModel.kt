package com.app.sino.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.RetrofitClient
import com.app.sino.data.remote.dto.UserDto
import com.app.sino.data.repository.UserRepository
import com.app.sino.data.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(RetrofitClient.userApi)
    private val tokenManager = TokenManager(application)

    private val _userState = MutableStateFlow<UserDto?>(null)
    val userState = _userState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = tokenManager.getToken()

        val email = tokenManager.getEmail()
        
        if (email != null) {
            viewModelScope.launch {
                _loading.value = true
                userRepository.syncUser(UserDto(email = email)).collect { result ->
                    if (result is Resource.Success) {
                        _userState.value = result.data
                    }
                    _loading.value = false
                }
            }
        }
    }
}
