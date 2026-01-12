package com.app.sino.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.sino.R
import com.app.sino.ui.components.SinoButton
import com.app.sino.ui.components.SinoScreenWrapper
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.SinoBlack
import com.app.sino.ui.theme.SinoWhite

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    SinoScreenWrapper(backgroundImageRes = R.drawable.bg_1) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.PaddingLarge)
        ) {
            // Top Section
            Spacer(modifier = Modifier.height(100.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sino_logo),
                    contentDescription = "SINO Logo",
                    modifier = Modifier.width(245.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillWidth,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(SinoWhite)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Your academic journey",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SinoWhite.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Log In Button (Black Filled)
                SinoButton(
                    text = "Log In",
                    onClick = onLoginClick,
                    containerColor = SinoBlack,
                    contentColor = SinoWhite
                )
                
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                // Or Label
                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp // Keeping this one-off or could move to Dimens
                    ),
                    color = SinoWhite.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                // Create Account Button (Outlined)
                SinoButton(
                    text = "Create Account",
                    onClick = onSignUpClick,
                    isOutlined = true,
                    contentColor = SinoWhite,
                    borderColor = SinoWhite
                )
                
                // Reduced spacing for Google button
                Spacer(modifier = Modifier.height(12.dp)) 

                // Google Button (White with Border)
                Button(
                    onClick = onGoogleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SinoWhite,
                        contentColor = SinoBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    shape = RoundedCornerShape(Dimens.ButtonCornerRadius)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.google_icon_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Unspecified 
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        onLoginClick = {},
        onSignUpClick = {},
        onGoogleClick = {}
    )
}