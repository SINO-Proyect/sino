package com.app.sino.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.util.Resource
import com.app.sino.ui.components.SinoBottomCard
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoScreenWrapper
import com.app.sino.ui.components.SinoTextField
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val authState = viewModel.authState.collectAsState().value
    val registerFormState by viewModel.registerFormState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                is AuthViewModel.ValidationEvent.Success -> {
                    onSignUpSuccess()
                }
                is AuthViewModel.ValidationEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        SinoScreenWrapper(backgroundImageRes = R.drawable.bg4) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top Section (Header)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.HeaderTopPadding, bottom = Dimens.HeaderBottomPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SinoBlack
                    )
                }

                // Bottom Section (Container)
                SinoBottomCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.PaddingHuge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Scrollable Fields Section
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Name Field
                            SinoTextField(
                                label = "Full Name",
                                value = name,
                                onValueChange = { 
                                    name = it
                                    viewModel.clearRegisterErrors()
                                },
                                placeholder = "Your Name",
                                isError = registerFormState.nameError != null,
                                errorMessage = registerFormState.nameError
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Email Field
                            SinoTextField(
                                label = "Email Address",
                                value = email,
                                onValueChange = { 
                                    email = it
                                    viewModel.clearRegisterErrors()
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                placeholder = "example@email.com",
                                isError = registerFormState.emailError != null,
                                errorMessage = registerFormState.emailError
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Password Field
                            SinoTextField(
                                label = "Password",
                                value = password,
                                onValueChange = { 
                                    password = it
                                    viewModel.clearRegisterErrors()
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = Icons.Default.Face,
                                onTrailingIconClick = { isPasswordVisible = !isPasswordVisible },
                                placeholder = "••••••••",
                                isError = registerFormState.passwordError != null,
                                errorMessage = registerFormState.passwordError
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Confirm Password Field
                            SinoTextField(
                                label = "Confirm Password",
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    viewModel.clearRegisterErrors()
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = Icons.Default.Face,
                                onTrailingIconClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                                placeholder = "••••••••",
                                isError = registerFormState.confirmPasswordError != null,
                                errorMessage = registerFormState.confirmPasswordError
                            )
                            
                            Spacer(modifier = Modifier.height(Dimens.PaddingGiga))
                        }

                        // Sign Up Button - Pushed to bottom by weight(1f) above
                        if (authState is Resource.Loading) {
                            CircularProgressIndicator(color = SinoWhite)
                        } else {
                            SinoButton(
                                text = "Sign Up",
                                onClick = {
                                    viewModel.register(email, password, confirmPassword, name, "")
                                },
                                containerColor = SinoWhite,
                                contentColor = SinoBlack,
                                enabled = true
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onSignUpSuccess = {}
    )
}
