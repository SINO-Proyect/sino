package com.app.sino.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.util.Resource
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoScreenWrapper
import com.app.sino.ui.theme.SinoPrimary

@Composable
fun VerifyEmailScreen(
    onContinueClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val email = viewModel.getUserEmail() ?: "tu correo registrado"
    val authState = viewModel.authState.collectAsState().value
    val resendCooldown by viewModel.resendCooldown.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                is AuthViewModel.ValidationEvent.Success -> {
                    snackbarHostState.showSnackbar("¡Correo de verificación enviado!")
                }
                is AuthViewModel.ValidationEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(SinoPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    modifier = Modifier.size(48.dp),
                    tint = SinoPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Verifica tu correo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Para completar tu registro, por favor haz clic en el enlace que enviamos a:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = email,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))

            SinoButton(
                text = "Ya he verificado mi correo",
                onClick = onContinueClick
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            TextButton(
                onClick = { viewModel.sendVerificationEmail() },
                enabled = authState !is Resource.Loading && resendCooldown == 0
            ) {
                Text(
                    text = if (resendCooldown > 0) "Reenviar disponible en ${resendCooldown}s" else "¿No recibiste el correo? Reenviar",
                    color = if (resendCooldown > 0) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else SinoPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}