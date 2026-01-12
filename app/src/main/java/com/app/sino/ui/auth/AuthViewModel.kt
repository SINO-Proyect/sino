package com.app.sino.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.sino.data.local.TokenManager
import com.app.sino.data.remote.RetrofitClient
import com.app.sino.data.remote.dto.LoginRequest
import com.app.sino.data.remote.dto.RegisterRequest
import com.app.sino.data.repository.AuthRepository
import com.app.sino.data.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(
        RetrofitClient.api,
        TokenManager(application)
    )

    private val _authState = MutableStateFlow<Resource<Unit>?>(null)
    val authState = _authState.asStateFlow()

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState = _loginFormState.asStateFlow()

    private val _registerFormState = MutableStateFlow(RegisterFormState())
    val registerFormState = _registerFormState.asStateFlow()

    private val _forgotPasswordFormState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordFormState = _forgotPasswordFormState.asStateFlow()

    private val _validationEvent = Channel<ValidationEvent>()
    val validationEvent = _validationEvent.receiveAsFlow()

    private val _infoMessage = Channel<String>()
    val infoMessage = _infoMessage.receiveAsFlow()

    private val _resendCooldown = MutableStateFlow(0)
    val resendCooldown = _resendCooldown.asStateFlow()

    init {
        checkAuth()
    }

    fun startResendCooldown() {
        if (_resendCooldown.value > 0) return
        viewModelScope.launch {
            _resendCooldown.value = 60
            while (_resendCooldown.value > 0) {
                delay(1000)
                _resendCooldown.value--
            }
        }
    }

    fun setInfoMessage(message: String) {
        viewModelScope.launch {
            _infoMessage.send(message)
        }
    }

    fun getUserEmail(): String? {
        return repository.getEmail()
    }

    fun isEmailVerified(): Boolean {
        return repository.isEmailVerified()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            repository.checkAuth().collect { isAuthenticated ->
                if (isAuthenticated) {
                    _authState.value = Resource.Success(Unit)
                } else {
                    _authState.value = Resource.Error("Unauthenticated")
                }
            }
        }
    }


    fun clearLoginErrors() {
        _loginFormState.value = LoginFormState()
    }

    fun clearRegisterErrors() {
        _registerFormState.value = RegisterFormState()
    }

    fun clearForgotPasswordErrors() {
        _forgotPasswordFormState.value = ForgotPasswordState()
    }

    fun isLoginValid(email: String, password: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6
    }

    fun isRegisterValid(email: String, password: String, confirm: String, name: String): Boolean {
        return email.isNotBlank() && 
               Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
               password.length >= 6 && 
               password == confirm && 
               name.isNotBlank()
    }

    fun login(email: String, password: String) {
        val emailError = if (email.isBlank()) "Please enter your email address." else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Please enter a valid email address." else null
        val passwordError = if (password.isBlank()) "Please enter your password." else null

        if (emailError != null || passwordError != null) {
            _loginFormState.value = LoginFormState(emailError = emailError, passwordError = passwordError)
            return
        }


        _loginFormState.value = LoginFormState()

        viewModelScope.launch {
            _authState.value = Resource.Loading()
            repository.login(LoginRequest(email, password)).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _authState.value = Resource.Success(Unit)
                        _validationEvent.send(ValidationEvent.Success)
                    }
                    is Resource.Error -> {
                        _authState.value = Resource.Error(result.message ?: "Login failed")
                        _validationEvent.send(ValidationEvent.Error(result.message ?: "Login failed"))
                    }
                    is Resource.Loading -> {
                        _authState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, displayName: String, phone: String) {
        val emailError = if (email.isBlank()) "Please enter your email address." else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "The email format is incorrect." else null
        val passwordError = if (password.length < 6) "Password must be at least 6 characters long." else null
        val confirmPasswordError = if (password != confirmPassword) "The passwords do not match." else null
        val nameError = if (displayName.isBlank()) "Please enter your full name." else null

        if (emailError != null || passwordError != null || confirmPasswordError != null || nameError != null) {
            _registerFormState.value = RegisterFormState(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                nameError = nameError
            )
            return
        }
        
        _registerFormState.value = RegisterFormState()

        viewModelScope.launch {
            _authState.value = Resource.Loading()
            repository.register(RegisterRequest(email, password, displayName, phone)).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _authState.value = Resource.Success(Unit)
                         _validationEvent.send(ValidationEvent.Success)
                    }
                    is Resource.Error -> {
                         _authState.value = Resource.Error(result.message ?: "Registration failed")
                         _validationEvent.send(ValidationEvent.Error(result.message ?: "Registration failed"))
                    }
                    is Resource.Loading -> _authState.value = Resource.Loading()
                }
            }
        }
    }

    fun isRecoverValid(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun recoverPassword(email: String) {
         val emailError = if (email.isBlank()) "Please enter your email address." else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Please enter a valid email address." else null
         
         if (emailError != null) {
             _forgotPasswordFormState.value = ForgotPasswordState(emailError = emailError)
             return
         }

         _forgotPasswordFormState.value = ForgotPasswordState()

         viewModelScope.launch {
            repository.recoverPassword(email).collect { result ->
                if (result is Resource.Success) {
                     _validationEvent.send(ValidationEvent.Success)
                } else if (result is Resource.Error) {
                    _validationEvent.send(ValidationEvent.Error(result.message ?: "Error"))
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout().collect {

            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            repository.sendVerificationEmail().collect { result ->
                if (result is Resource.Success) {
                    _validationEvent.send(ValidationEvent.Success)
                    startResendCooldown()
                } else if (result is Resource.Error) {
                    // If rate limited (429), also start cooldown to prevent further spam
                    if (result.message?.contains("Too many requests") == true) {
                        startResendCooldown()
                    }
                    _validationEvent.send(ValidationEvent.Error(result.message ?: "Error sending email"))
                }
            }
        }
    }

    sealed class ValidationEvent {
        object Success : ValidationEvent()
        data class Error(val message: String) : ValidationEvent()
    }
}

data class LoginFormState(
    val emailError: String? = null,
    val passwordError: String? = null
)

data class RegisterFormState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nameError: String? = null
)

data class ForgotPasswordState(
    val emailError: String? = null
)
