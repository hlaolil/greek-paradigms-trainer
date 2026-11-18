package com.example.greekparadigms.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Greek is always set in the serif face, which on Android covers polytonic Greek. */
val GreekFamily = FontFamily.Serif

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = GreekFamily),
    displayMedium = base.displayMedium.copy(fontFamily = GreekFamily),
    displaySmall = base.displaySmall.copy(fontFamily = GreekFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = GreekFamily, fontWeight = FontWeight.Medium),
    headlineMedium = base.headlineMedium.copy(fontFamily = GreekFamily, fontWeight = FontWeight.Medium),
    headlineSmall = base.headlineSmall.copy(fontFamily = GreekFamily, fontWeight = FontWeight.Medium),
    titleLarge = base.titleLarge.copy(fontFamily = GreekFamily, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
)

/** Style for a Greek word shown as the answer or prompt of a card. */
val GreekCardStyle = TextStyle(fontFamily = GreekFamily, fontSize = 40.sp, lineHeight = 48.sp)
