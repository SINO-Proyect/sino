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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.sino.R
import com.app.sino.ui.model.BadgePalette
import com.app.sino.ui.model.CourseStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseBadge(
    courseName: String,
    courseCode: String,
    status: CourseStatus,
    palette: BadgePalette,
    prerequisitesCodes: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val badgeSize = 110.dp
    val mainCircleSize = 76.dp

    val isProgress = status == CourseStatus.IN_PROGRESS
    val isLocked = status == CourseStatus.LOCKED
    val isPassed = status == CourseStatus.PASSED
    val isAvailable = status == CourseStatus.AVAILABLE

    // Colors derived from the specific palette for this period/level
    val baseColor = if (isLocked) Color(0xFF64748B) else palette.main
    val statusColor = when {
        isPassed -> Color(0xFF10B981) // Consistent Emerald for Passed
        isProgress -> palette.main
        isAvailable -> palette.main.copy(alpha = 0.8f)
        else -> baseColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(badgeSize)
        ) {
            // Glow effect for active states
            if (!isLocked) {
                Box(
                    modifier = Modifier
                        .size(mainCircleSize + 8.dp)
                        .background(statusColor.copy(alpha = 0.1f), CircleShape)
                )
            }

            // Main Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(mainCircleSize)
                    .clip(CircleShape)
                    .background(if (isLocked) Color(0xFF0F172A) else Color(0xFF020617))
                    .border(
                        width = 1.dp,
                        color = statusColor.copy(alpha = 0.5f),
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
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Code Label (Glassmorphism effect)
            Surface(
                color = statusColor.copy(alpha = 0.9f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
            ) {
                Text(
                    text = courseCode,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = courseName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                lineHeight = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            ),
            color = if (isLocked) Color.Gray else Color.White,
            maxLines = 2,
            modifier = Modifier.width(100.dp)
        )

        if (isLocked && prerequisitesCodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(110.dp)
            ) {
                prerequisitesCodes.forEach { code ->
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = code,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
