package com.app.sino.ui.model

import androidx.compose.ui.graphics.Color

enum class CourseStatus {
    PASSED,           // Ganado
    IN_PROGRESS,      // Cursandose
    AVAILABLE,        // Desbloqueado pero no matriculado
    LOCKED            // Bloqueado por requisitos
}

data class BadgePalette(
    val main: Color,
    val sub1: Color,
    val sub2: Color,
    val sub3: Color,
    val sub4: Color
)

object PathTheme {
    // Apple-style Emerald/Green Palettes
    val accentColors = listOf(
        BadgePalette(Color(0xFF34C759), Color(0xFF30D158), Color(0xFF248A3D), Color(0xFF1E6F31), Color(0xFFE3F9E5)), // Apple Green
        BadgePalette(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857), Color(0xFF065F46), Color(0xFFD1FAE5)), // Emerald
        BadgePalette(Color(0xFF32D74B), Color(0xFF28A745), Color(0xFF218838), Color(0xFF1E7E34), Color(0xFFD4EDDA))  // System Green
    )

    val grayPalette = BadgePalette(
        main = Color(0xFF3A3A3C), // Apple Dark Gray
        sub1 = Color(0xFF2C2C2E),
        sub2 = Color(0xFF1C1C1E),
        sub3 = Color(0xFF000000),
        sub4 = Color(0xFF8E8E93)
    )

    fun getBadgePalette(index: Int): BadgePalette {
        // If the user wants uniformity, we can just return one or cycle through very similar greens
        return accentColors[index % accentColors.size]
    }
}