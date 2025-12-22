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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.sino.R
import com.app.sino.ui.components.SinoBottomCard
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoScreenWrapper
import com.app.sino.ui.components.SinoTextField
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    SinoScreenWrapper(backgroundImageRes = R.drawable.bg2) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section (Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.HeaderTopPadding, bottom = Dimens.HeaderBottomPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Crear Cuenta",
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
                            label = "Nombre Completo",
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Tu nombre"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email Field
                        SinoTextField(
                            label = "Correo electrónico",
                            value = email,
                            onValueChange = { email = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = "ejemplo@correo.com"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Password Field
                        SinoTextField(
                            label = "Contraseña",
                            value = password,
                            onValueChange = { password = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = Icons.Default.Face,
                            onTrailingIconClick = { isPasswordVisible = !isPasswordVisible },
                            placeholder = "••••••••"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Confirm Password Field
                        SinoTextField(
                            label = "Confirmar Contraseña",
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = Icons.Default.Face,
                            onTrailingIconClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                            placeholder = "••••••••"
                        )
                        
                        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
                    }

                    // Sign Up Button - Pushed to bottom by weight(1f) above
                    SinoButton(
                        text = "Registrarse",
                        onClick = onSignUpSuccess,
                        containerColor = SinoWhite,
                        contentColor = SinoBlack
                    )
                    
                    Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
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
