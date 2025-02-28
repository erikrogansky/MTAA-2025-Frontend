package com.roganskyerik.cookly.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.roganskyerik.cookly.R

val Nunito = FontFamily(
    Font(R.font.nunito_extra_light, FontWeight.ExtraLight),
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semi_bold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extra_bold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black)
)

//val NunitoItalic = FontFamily(
//    Font(R.font.nunito_extra_light_italic, FontWeight.ExtraLight),
//    Font(R.font.nunito_light_italic, FontWeight.Light),
//    Font(R.font.nunito_italic, FontWeight.Normal),
//    Font(R.font.nunito_medium_italic, FontWeight.Medium),
//    Font(R.font.nunito_semi_bold_italic, FontWeight.SemiBold),
//    Font(R.font.nunito_bold_italic, FontWeight.Bold),
//    Font(R.font.nunito_extra_bold_italic, FontWeight.ExtraBold),
//    Font(R.font.nunito_black_italic, FontWeight.Black)
//)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    )
)