package io.github.hlaolil.greekparadigms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = InkContainer,
    onPrimaryContainer = OnInkContainer,
    secondary = Burgundy,
    onSecondary = Color.White,
    secondaryContainer = BurgundyContainer,
    onSecondaryContainer = OnBurgundyContainer,
    tertiary = Gold,
    onTertiary = Color.White,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = OnGoldContainer,
    background = Parchment,
    onBackground = Sepia,
    surface = Parchment,
    onSurface = Sepia,
    surfaceVariant = ParchmentHigh,
    onSurfaceVariant = SepiaVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F3EA),
    surfaceContainer = ParchmentDim,
    surfaceContainerHigh = ParchmentHigh,
    surfaceContainerHighest = Color(0xFFE2DACA),
    outline = Outline,
    outlineVariant = Color(0xFFD5CDBE),
)

private val DarkColors = darkColorScheme(
    primary = InkDark,
    onPrimary = Color(0xFF0B1F38),
    primaryContainer = InkContainerDark,
    onPrimaryContainer = Color(0xFFD6E3F7),
    secondary = BurgundyDark,
    onSecondary = Color(0xFF4A1219),
    secondaryContainer = BurgundyContainerDark,
    onSecondaryContainer = Color(0xFFF6D9DB),
    tertiary = GoldDark,
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = GoldContainer,
    background = NightBackground,
    onBackground = NightText,
    surface = NightBackground,
    onSurface = NightText,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightTextVariant,
    surfaceContainerLowest = Color(0xFF100F0B),
    surfaceContainerLow = NightSurface,
    surfaceContainer = Color(0xFF22211B),
    surfaceContainerHigh = NightSurfaceHigh,
    surfaceContainerHighest = Color(0xFF35332C),
    outline = Color(0xFF9A9284),
    outlineVariant = Color(0xFF4A463D),
)

/** Colours that Material's scheme has no slot for. */
@Immutable
data class ExtraColors(val mastered: Color, val learning: Color)

val LocalExtraColors = staticCompositionLocalOf { ExtraColors(MasteredLight, LearningLight) }

@Composable
fun GreekParadigmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val extra = if (darkTheme) ExtraColors(MasteredDark, LearningDark) else ExtraColors(MasteredLight, LearningLight)
    CompositionLocalProvider(LocalExtraColors provides extra) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}
