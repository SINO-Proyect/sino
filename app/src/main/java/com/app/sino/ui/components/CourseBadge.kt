package com.app.sino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.sino.R
import com.app.sino.ui.model.BadgePalette
import com.app.sino.ui.model.CourseStatus
import com.app.sino.ui.theme.EmeraldDeep
import com.app.sino.ui.theme.SinoPrimary

@Composable
fun CourseBadge(
    courseName: String,
    courseCode: String,
    status: CourseStatus,
    palette: BadgePalette,
    prerequisitesCodes: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val isProgress = status == CourseStatus.IN_PROGRESS
    val isLocked = status == CourseStatus.LOCKED
    val isPassed = status == CourseStatus.PASSED
    val isAvailable = status == CourseStatus.AVAILABLE

    val badgeColor = when {
        isPassed -> SinoPrimary
        isProgress -> Color(0xFF32D74B)
        isAvailable -> Color.White.copy(alpha = 0.9f)
        else -> Color(0xFF2C2C2E)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        // Círculo Grande (88dp) con colores originales
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .then(
                    if (isProgress) Modifier.shadow(16.dp, CircleShape, spotColor = badgeColor)
                    else Modifier
                )
                .clip(CircleShape)
                .background(
                    when {
                        isLocked -> Color(0xFF1C1C1E)
                        isPassed -> EmeraldDeep
                        isAvailable -> Color.White.copy(alpha = 0.05f)
                        else -> badgeColor
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = badgeColor.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(
                    id = when {
                        isLocked -> R.drawable.lock
                        isAvailable -> R.drawable.lock_open
                        isPassed -> R.drawable.check
                        else -> R.drawable.book_open_duotone
                    }
                ),
                contentDescription = null,
                tint = when {
                    isPassed -> SinoPrimary
                    isProgress -> Color.Black
                    isAvailable -> Color.White
                    else -> Color.White.copy(alpha = 0.15f)
                },
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del Curso (14sp)
        Text(
            text = courseName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                fontSize = 14.sp
            ),
            color = if (isLocked) Color.White.copy(alpha = 0.2f) else Color.White,
            maxLines = 2,
            minLines = 2,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Chip de Código (13sp)
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(50),
        ) {
            Text(
                text = courseCode,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isLocked) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
