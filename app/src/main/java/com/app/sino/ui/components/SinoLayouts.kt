package com.app.sino.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.util.Random

@Composable
fun SinoScreenWrapper(
    backgroundImageRes: Int? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

@Composable
fun SinoBottomCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
    ) {
        content()
    }
}

fun Modifier.grainEffect(alpha: Float = 0.05f): Modifier = this.drawWithCache {
    val noiseSize = 512
    val bitmap = Bitmap.createBitmap(noiseSize, noiseSize, Bitmap.Config.ARGB_8888)
    val pixels = IntArray(noiseSize * noiseSize)
    val random = Random()

    for (i in pixels.indices) {
        val gray = random.nextInt(256)
        pixels[i] = AndroidColor.argb(255, gray, gray, gray)
    }
    bitmap.setPixels(pixels, 0, noiseSize, 0, 0, noiseSize, noiseSize)
    val imageBitmap = bitmap.asImageBitmap()

    onDrawWithContent {
        drawContent()
        var y = 0
        while (y < size.height) {
            var x = 0
            while (x < size.width) {
                drawImage(
                    image = imageBitmap,
                    dstOffset = IntOffset(x, y),
                    alpha = alpha
                )
                x += noiseSize
            }
            y += noiseSize
        }
    }
}