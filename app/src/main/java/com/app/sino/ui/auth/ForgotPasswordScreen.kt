package com.app.sino.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.ui.components.SinoBottomCard
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoScreenWrapper
import com.app.sino.ui.components.SinoTextField
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@Composable
fun ForgotPasswordScreen(
    onResetClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val forgotPasswordFormState by viewModel.forgotPasswordFormState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                is AuthViewModel.ValidationEvent.Success -> {
                    snackbarHostState.showSnackbar("Recovery email sent!")
                    onResetClick()
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
        SinoScreenWrapper(backgroundImageRes = R.drawable.bg5) {
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
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SinoWhite // White for contrast on dark bg
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
                        
                        Text(
                            text = "Enter your email address below and we'll send you instructions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SinoBlack.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Email Field
                        SinoTextField(
                            label = "Email Address",
                            value = email,
                            onValueChange = { 
                                email = it
                                viewModel.clearForgotPasswordErrors()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = "example@email.com",
                            isError = forgotPasswordFormState.emailError != null,
                            errorMessage = forgotPasswordFormState.emailError
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        SinoButton(
                            text = "Send Instructions",
                            onClick = { viewModel.recoverPassword(email) },
                            containerColor = SinoWhite,
                            contentColor = SinoBlack,
                            enabled = true
                        )
                        
                        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                    }
                }
            }
        }
    }
}