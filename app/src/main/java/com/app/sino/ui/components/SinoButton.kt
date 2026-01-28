package com.app.sino.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.sino.ui.theme.PremiumGradientEnd
import com.app.sino.ui.theme.PremiumGradientStart
import com.app.sino.ui.theme.SinoPrimary
import com.app.sino.ui.theme.SinoWhite

@Composable
fun SinoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp) // Apple-style rounded corners

    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SinoPrimary,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (enabled) SinoPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            ),
            shape = shape,
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(shape),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black, // Dark text on light green button
                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            ),
            contentPadding = PaddingValues(0.dp),
            shape = shape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (enabled) Brush.horizontalGradient(
                            colors = listOf(PremiumGradientStart, PremiumGradientEnd)
                        ) else Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}