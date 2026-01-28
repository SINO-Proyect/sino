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
    val rainbowColors = listOf(
        BadgePalette(Color(0xFFE35D5B), Color(0xFF9E2A29), Color(0xFF7A1817), Color(0xFF520B0A), Color(0xFFF5C8C7)),
        BadgePalette(Color(0xFFF38B28), Color(0xFFB96F00), Color(0xFF915700), Color(0xFF613A00), Color(0xFFF8C890)),
        BadgePalette(Color(0xFFEBC347), Color(0xFF9C7D1A), Color(0xFF755D10), Color(0xFF4D3D08), Color(0xFFFBEFB9)),
        BadgePalette(Color(0xFF32A94D), Color(0xFF18682B), Color(0xFF1B5626), Color(0xFF0B3816), Color(0xFFB2DCA9)),
        BadgePalette(Color(0xFF4581BF), Color(0xFF275A8E), Color(0xFF164478), Color(0xFF072751), Color(0xFF9FD0EC)),
        BadgePalette(Color(0xFF874A97), Color(0xFF572E62), Color(0xFF531C5A), Color(0xFF360F3D), Color(0xFFC59CDB))
    )

    val grayPalette = BadgePalette(
        main = Color(0xFF4A5B64),
        sub1 = Color(0xFF3A474D),
        sub2 = Color(0xFF424242),
        sub3 = Color(0xFF212121),
        sub4 = Color(0xFFD3D3D3)
    )

    fun getBadgePalette(index: Int): BadgePalette {
        return rainbowColors[index % rainbowColors.size]
    }
}
