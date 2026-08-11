package com.dennis.bookora.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = OnBlue40,
    primaryContainer = BlueContainer90,
    onPrimaryContainer = OnBlueContainer10,
    secondary = SecondaryBlue40,
    onSecondary = OnSecondaryBlue40,
    secondaryContainer = SecondaryBlueContainer90,
    onSecondaryContainer = OnSecondaryBlueContainer10,
    tertiary = TertiaryBlue40,
    onTertiary = OnTertiaryBlue40,
    tertiaryContainer = TertiaryBlueContainer90,
    onTertiaryContainer = OnTertiaryBlueContainer10,
    error = Error40,
    onError = OnError40,
    errorContainer = ErrorContainer90,
    onErrorContainer = OnErrorContainer10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    outline = NeutralVariant30,
    surfaceVariant = NeutralVariant90
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = OnBlue20,
    primaryContainer = BlueContainer30,
    onPrimaryContainer = OnBlueContainer90,
    secondary = SecondaryBlue80,
    onSecondary = OnSecondaryBlue20,
    secondaryContainer = SecondaryBlueContainer30,
    onSecondaryContainer = OnSecondaryBlueContainer90,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    outline = NeutralVariant90,
    surfaceVariant = NeutralVariant30
)

@Composable
fun BookoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to enforce our blue theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}