package com.roganskyerik.cookly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


@Immutable
data class CooklyColors(
    val Orange100: Color,
    val FontColor: Color,
    val FontColorReverse: Color,
)

// Light theme colors
val LightCooklyColors = CooklyColors(
    Orange100 = Color(0xFFFFB200),
    FontColor = Color(0xFF000000),
    FontColorReverse = Color(0xFFFFFFFF),
)

// Dark theme colors
val DarkCooklyColors = CooklyColors(
    Orange100 = Color(0xFFFFB200),
    FontColor = Color(0xFFFFFFFF),
    FontColorReverse = Color(0xFF000000),
)
