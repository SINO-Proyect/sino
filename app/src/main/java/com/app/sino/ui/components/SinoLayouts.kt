package com.app.sino.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.app.sino.ui.theme.Dimens
import com.app.sino.ui.theme.GradientBlackEnd
import com.app.sino.ui.theme.GradientBlackStart
import com.app.sino.ui.theme.SinoWhite

@Composable
fun SinoScreenWrapper(
    backgroundImageRes: Int,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SinoWhite)
    ) {
        Image(
            painter = painterResource(id = backgroundImageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(Dimens.BlurRadius),
            contentScale = ContentScale.Crop
        )
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
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientBlackStart,
                        GradientBlackEnd
                    )
                ),
                shape = RoundedCornerShape(topStart = Dimens.BottomCardCornerRadius, topEnd = Dimens.BottomCardCornerRadius)
            )
    ) {
        content()
    }
}
