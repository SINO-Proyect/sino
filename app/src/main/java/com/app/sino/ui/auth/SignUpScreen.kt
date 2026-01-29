package com.app.sino.ui.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.util.Resource
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoTextField
import com.app.sino.ui.theme.SinoPrimary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val authState = viewModel.authState.collectAsState().value
    val registerFormState by viewModel.registerFormState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var apiError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                is AuthViewModel.ValidationEvent.Success -> {
                    onSignUpSuccess()
                }
                is AuthViewModel.ValidationEvent.Error -> {
                    apiError = event.message
                }
            }
        }
    }
    

    LaunchedEffect(name, email, password, confirmPassword) {
         if (apiError != null) apiError = null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Únete a SINO y empieza a planificar tu futuro.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SinoTextField(
                    label = "Nombre Completo",
                    value = name,
                    onValueChange = { 
                        name = it
                        viewModel.clearRegisterErrors()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    placeholder = "Juan Pérez",
                    isError = registerFormState.nameError != null,
                    errorMessage = registerFormState.nameError
                )

                SinoTextField(
                    label = "Nombre de Usuario",
                    value = username,
                    onValueChange = { 
                        username = it
                        viewModel.clearRegisterErrors()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    placeholder = "juanperez",
                    isError = registerFormState.usernameError != null,
                    errorMessage = registerFormState.usernameError
                )

                SinoTextField(
                    label = "Correo Electrónico",
                    value = email,
                    onValueChange = { 
                        email = it
                        viewModel.clearRegisterErrors()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    placeholder = "tu@email.com",
                    isError = registerFormState.emailError != null,
                    errorMessage = registerFormState.emailError
                )

                SinoTextField(
                    label = "Contraseña",
                    value = password,
                    onValueChange = { 
                        password = it
                        viewModel.clearRegisterErrors()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIconRes = if (isPasswordVisible) R.drawable.eye_bold else R.drawable.eye_closed_bold,
                    onTrailingIconClick = { isPasswordVisible = !isPasswordVisible },
                    placeholder = "••••••••",
                    isError = registerFormState.passwordError != null,
                    errorMessage = registerFormState.passwordError
                )

                SinoTextField(
                    label = "Confirmar Contraseña",
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        viewModel.clearRegisterErrors()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.register(email, password, confirmPassword, name, username, "") }
                    ),
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIconRes = if (isConfirmPasswordVisible) R.drawable.eye_bold else R.drawable.eye_closed_bold,
                    onTrailingIconClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                    placeholder = "••••••••",
                    isError = registerFormState.confirmPasswordError != null,
                    errorMessage = registerFormState.confirmPasswordError
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (apiError != null) {
                Text(
                    text = apiError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }

            val isSignUpEnabled = viewModel.isRegisterValid(email, password, confirmPassword, name, username)

            SinoButton(
                text = if (authState is Resource.Loading) "Creando cuenta..." else "Registrarse",
                onClick = {
                    viewModel.register(email, password, confirmPassword, name, username, "")
                },
                enabled = isSignUpEnabled && authState !is Resource.Loading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
