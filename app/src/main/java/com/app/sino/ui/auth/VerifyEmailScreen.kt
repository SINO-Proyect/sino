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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.sino.R
import com.app.sino.data.util.Resource
import com.app.sino.ui.components.SinoBottomCard
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoScreenWrapper
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@Composable
fun VerifyEmailScreen(
    onContinueClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val email = viewModel.getUserEmail() ?: "your registered email"
    val authState = viewModel.authState.collectAsState().value
    val resendCooldown by viewModel.resendCooldown.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.validationEvent.collect { event ->
            when (event) {
                is AuthViewModel.ValidationEvent.Success -> {
                    snackbarHostState.showSnackbar("Verification email sent!")
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
        SinoScreenWrapper(backgroundImageRes = R.drawable.bg3) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.HeaderTopPadding, bottom = Dimens.HeaderBottomPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Verify Email",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SinoBlack
                    )
                }


                SinoBottomCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.PaddingHuge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(SinoWhite.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                modifier = Modifier.size(48.dp),
                                tint = SinoWhite
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Check your inbox",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SinoWhite
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "To complete your registration, please click on the link we sent to:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SinoWhite.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = email,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SinoWhite,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))

                        SinoButton(
                            text = "I've verified my email",
                            onClick = onContinueClick,
                            containerColor = SinoWhite,
                            contentColor = SinoBlack
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        TextButton(
                            onClick = { viewModel.sendVerificationEmail() },
                            enabled = authState !is Resource.Loading && resendCooldown == 0
                        ) {
                            Text(
                                text = if (resendCooldown > 0) "Resend available in ${resendCooldown}s" else "Didn't get the code? Resend",
                                color = if (resendCooldown > 0) SinoWhite.copy(alpha = 0.4f) else SinoWhite.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
                    }
                }
            }
        }
    }
}
