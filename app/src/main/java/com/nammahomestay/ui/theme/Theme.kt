package com.nammahomestay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Terracotta,
    secondary = ForestGreen,
    tertiary = TerracottaVariant,
    background = Color(0xFF1E1E1E),
    surface = Color(0xFF2C2C2C),
    surfaceVariant = Color(0xFF3C3C3C) // Dark mode equivalent for FFFAF4
)

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    secondary = ForestGreen,
    tertiary = TerracottaVariant,
    background = WarmCream,
    surface = Color.White,
    surfaceVariant = Color(0xFFFFFAF4), // Card background requested
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF3D2B1F),
    onSurface = Color(0xFF3D2B1F)
)

val GlobalShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp), // All cards with 12dp corners
    large = RoundedCornerShape(24.dp) // All buttons with 24dp corner radius
)

@Composable
fun NammaHomeStayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.tertiary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = GlobalShapes,
        content = content
    )
}
