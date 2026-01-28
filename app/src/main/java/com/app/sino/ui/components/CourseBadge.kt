package com.app.sino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun CourseBadge(
    courseName: String,
    courseCode: String,
    status: CourseStatus,
    palette: BadgePalette,
    requirementsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val badgeSize = 112.dp
    val mainCircleSize = 80.dp

    val contentAlpha = when (status) {
        CourseStatus.PASSED -> 0.5f
        CourseStatus.LOCKED -> 0.6f
        CourseStatus.AVAILABLE -> 1.0f
        else -> 1.0f
    }

    val isProgress = status == CourseStatus.IN_PROGRESS
    val isLocked = status == CourseStatus.LOCKED
    val isPassed = status == CourseStatus.PASSED
    val isAvailable = status == CourseStatus.AVAILABLE

    val mainCircleColor = when {
        isLocked || isAvailable -> palette.main
        isPassed -> palette.sub2
        else -> palette.main
    }

    val shadowColor = when {
        isLocked || isAvailable -> palette.sub1
        isPassed -> palette.sub3
        else -> palette.sub1
    }
    
    val mainIconColor = when {
        isProgress || isPassed -> palette.sub4
        isLocked || isAvailable -> palette.sub4
        else -> Color.White
    }
    
    val idBadgeBgColor = when {
        isProgress || isPassed -> palette.sub4
        isLocked || isAvailable -> palette.sub4
        else -> palette.main.copy(alpha = 0.3f)
    }
    val idBadgeTextColor = when {
        isProgress || isPassed -> palette.sub2
        isLocked || isAvailable -> palette.sub2
        else -> Color.Black
    }

    val topDecorationBgColor = when {
        isProgress || isPassed -> palette.sub4
        isAvailable -> palette.sub4
        else -> palette.main
    }
    val topDecorationIconColor = when {
        isProgress || isPassed -> palette.sub2
        isAvailable -> palette.sub2
        else -> Color.White
    }

    val showTopDecoration = isProgress || isAvailable || isPassed

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(badgeSize)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .offset(y = 7.dp)
                    .size(mainCircleSize)
                    .clip(CircleShape)
                    .background(shadowColor)
            )

            // Main Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(mainCircleSize)
                    .clip(CircleShape)
                    .background(mainCircleColor)
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = mainIconColor,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                     Icon(
                        painter = painterResource(id = R.drawable.book_open_duotone),
                        contentDescription = null,
                        tint = mainIconColor,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Top Decoration
            if (showTopDecoration) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 2.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(topDecorationBgColor)
                ) {
                    if (isProgress) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_path_filled), // Using path/book icon for progress
                            contentDescription = null,
                            tint = topDecorationIconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isAvailable) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = topDecorationIconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = topDecorationIconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom Pill (Course Code)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
                    .background(
                        color = idBadgeBgColor,
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = courseCode,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = idBadgeTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = courseName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 2,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(100.dp)
        )

        if (isLocked && requirementsCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = palette.sub2,
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_path_outline),
                        contentDescription = null,
                        tint = palette.sub4,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$requirementsCount Req",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White
                    )
                }
            }
        }
    }
}
