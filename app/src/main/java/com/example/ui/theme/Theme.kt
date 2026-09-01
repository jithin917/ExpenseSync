package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreenLight,
    onPrimary = BrandGreenDark,
    primaryContainer = BrandGreen,
    onPrimaryContainer = Color.White,
    secondary = BrandGreenTint,
    onSecondary = BrandGreenDark,
    secondaryContainer = Color(0xFF1E3821),
    onSecondaryContainer = BrandGreenLight,
    tertiary = ExpenseRedLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = PolishBackground,
    onSurface = PolishBackground,
    surfaceVariant = Color(0xFF263326),
    onSurfaceVariant = PolishBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    primaryContainer = BrandGreenLight,
    onPrimaryContainer = BrandGreenDark,
    secondary = BrandGreenTint,
    onSecondary = BrandGreenDark,
    secondaryContainer = PolishNavBackground,
    onSecondaryContainer = PolishTextPrimary,
    tertiary = ExpenseRed,
    background = PolishBackground,
    surface = PolishSurface,
    onBackground = PolishTextPrimary,
    onSurface = PolishTextPrimary,
    surfaceVariant = PolishHover,
    onSurfaceVariant = PolishTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content
    )
}

