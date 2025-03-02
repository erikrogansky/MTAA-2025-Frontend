package com.roganskyerik.cookly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color


@Immutable
data class CooklyColors(
    val Orange100: Color,
    val FontColor: Color,
    val FontDark: Color,

    val LinkColor: Color,

    val DarkOrange: Color,

    val Error: Color,
)

// Light theme colors
val LightCooklyColors = CooklyColors(
    Orange100 = Color(0xFFFFB200),
    FontColor = Color(0xFF481E00),
    FontDark = Color(0xFF481E00),

    LinkColor = Color(0xFFBB6002),

    DarkOrange = Color(0xFFE28900),

    Error = Color(0xFFD90000),
)

// Dark theme colors
val DarkCooklyColors = CooklyColors(
    Orange100 = Color(0xFFFFB200),
    FontColor = Color(0xFFFFFFFF),
    FontDark = Color(0xFF481E00),

    LinkColor = Color(0xFFBB6002),

    DarkOrange = Color(0xFFE28900),

    Error = Color(0xFFD90000),
)
