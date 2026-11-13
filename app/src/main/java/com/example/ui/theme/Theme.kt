package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class WhatsProBrandPalette(
    val deepBlack: Color,
    val deepBlackElevated: Color,
    val electricBlue: Color,
    val electricBlueContainer: Color,
    val neoRed: Color,
    val neoRedContainer: Color,
    val dashboardGreen: Color,
    val dashboardGreenDark: Color,
    val dashboardGreenContainer: Color,
    val subtleBorder: Color
)

private val LightBrandPalette = WhatsProBrandPalette(
    deepBlack = WhatsProDeepBlack,
    deepBlackElevated = WhatsProDeepBlackSurface,
    electricBlue = WhatsProElectricBlue,
    electricBlueContainer = WhatsProElectricBlueContainer,
    neoRed = WhatsProNeoRed,
    neoRedContainer = WhatsProNeoRedContainer,
    dashboardGreen = WhatsProEmerald,
    dashboardGreenDark = WhatsProEmeraldDark,
    dashboardGreenContainer = WhatsProMintContainer,
    subtleBorder = WhatsProBorder
)

private val DarkBrandPalette = WhatsProBrandPalette(
    deepBlack = WhatsProDeepBlack,
    deepBlackElevated = WhatsProDeepBlackSurface,
    electricBlue = WhatsProElectricBlue,
    electricBlueContainer = WhatsProOnElectricBlue,
    neoRed = WhatsProNeoRed,
    neoRedContainer = WhatsProOnNeoRed,
    dashboardGreen = WhatsProEmeraldBright,
    dashboardGreenDark = WhatsProEmerald,
    dashboardGreenContainer = Color(0xFF00382B),
    subtleBorder = Color(0xFF263044)
)

val LocalWhatsProBrandPalette = staticCompositionLocalOf { LightBrandPalette }

private val LightColorScheme = lightColorScheme(
    primary = WhatsProElectricBlue,
    onPrimary = Color.White,
    primaryContainer = WhatsProElectricBlueContainer,
    onPrimaryContainer = WhatsProOnElectricBlue,
    secondary = WhatsProDeepBlack,
    onSecondary = Color.White,
    secondaryContainer = WhatsProSurfaceVariant,
    onSecondaryContainer = WhatsProDeepBlack,
    tertiary = WhatsProNeoRed,
    onTertiary = Color.White,
    tertiaryContainer = WhatsProNeoRedContainer,
    onTertiaryContainer = WhatsProOnNeoRed,
    background = WhatsProBackground,
    onBackground = WhatsProDeepBlack,
    surface = WhatsProSurface,
    onSurface = WhatsProDeepBlack,
    surfaceVariant = WhatsProSurfaceVariant,
    onSurfaceVariant = WhatsProInkMuted,
    surfaceContainerLow = WhatsProBackground,
    surfaceContainer = WhatsProSurfaceVariant,
    surfaceContainerHigh = Color(0xFFE5ECF6),
    outline = WhatsProBorder,
    outlineVariant = WhatsProBorder,
    error = WhatsProNeoRed,
    onError = Color.White,
    errorContainer = WhatsProNeoRedContainer,
    onErrorContainer = WhatsProOnNeoRed
)

private val DarkColorScheme = darkColorScheme(
    primary = WhatsProElectricBlue,
    onPrimary = Color.White,
    primaryContainer = WhatsProOnElectricBlue,
    onPrimaryContainer = WhatsProElectricBlueContainer,
    secondary = WhatsProDeepBlackMuted,
    onSecondary = Color.White,
    secondaryContainer = WhatsProDeepBlackSurface,
    onSecondaryContainer = WhatsProDarkInk,
    tertiary = WhatsProNeoRed,
    onTertiary = Color.White,
    tertiaryContainer = WhatsProOnNeoRed,
    onTertiaryContainer = WhatsProNeoRedContainer,
    background = WhatsProDarkBg,
    onBackground = WhatsProDarkInk,
    surface = WhatsProDarkSurface,
    onSurface = WhatsProDarkInk,
    surfaceVariant = WhatsProDarkSurfaceVariant,
    onSurfaceVariant = WhatsProDarkInkMuted,
    surfaceContainerLow = WhatsProDarkBg,
    surfaceContainer = WhatsProDarkSurface,
    surfaceContainerHigh = WhatsProDarkSurfaceVariant,
    outline = Color(0xFF263044),
    outlineVariant = Color(0xFF1E2638),
    error = WhatsProNeoRed,
    onError = Color.White,
    errorContainer = WhatsProOnNeoRed,
    onErrorContainer = WhatsProNeoRedContainer
)

val WhatsProShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object WhatsProBrandTheme {
    val palette: WhatsProBrandPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalWhatsProBrandPalette.current
}

@Composable
fun WhatsProTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val brandPalette = if (darkTheme) DarkBrandPalette else LightBrandPalette

    CompositionLocalProvider(LocalWhatsProBrandPalette provides brandPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WhatsProTypography,
            shapes = WhatsProShapes,
            content = content
        )
    }
}
