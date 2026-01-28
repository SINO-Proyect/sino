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
    val accentColors = listOf(
        BadgePalette(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF4338CA), Color(0xFF3730A3), Color(0xFFE0E7FF)), // Indigo
        BadgePalette(Color(0xFF06B6D4), Color(0xFF0891B2), Color(0xFF0E7490), Color(0xFF155E75), Color(0xFFCFFAFE)), // Cyan
        BadgePalette(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857), Color(0xFF065F46), Color(0xFFD1FAE5)), // Emerald
        BadgePalette(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309), Color(0xFF92400E), Color(0xFFFEF3C7)), // Amber
        BadgePalette(Color(0xFF8B5CF6), Color(0xFF7C3AED), Color(0xFF6D28D9), Color(0xFF5B21B6), Color(0xFFEDE9FE))  // Violet
    )

    val grayPalette = BadgePalette(
        main = Color(0xFF334155),
        sub1 = Color(0xFF1E293B),
        sub2 = Color(0xFF0F172A),
        sub3 = Color(0xFF020617),
        sub4 = Color(0xFF94A3B8)
    )

    fun getBadgePalette(index: Int): BadgePalette {
        return accentColors[index % accentColors.size]
    }
}
