package com.nammahomestay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Terra,
    secondary = Leaf,
    tertiary = Brown,
    background = Color(0xFF1C110A),
    surface = Color(0xFF2A1200),
    surfaceVariant = Color(0xFF3E1C00),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Cream,
    onSurface = Cream
)

private val LightColorScheme = lightColorScheme(
    primary = Terra,
    secondary = Leaf,
    tertiary = Brown,
    background = Cream,
    surface = Color.White,
    surfaceVariant = Sand,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = TerraXL
)

val GlobalShapes = Shapes(
    small = RoundedCornerShape(10.dp), // --r-s for buttons/inputs
    medium = RoundedCornerShape(16.dp), // --r for cards
    large = RoundedCornerShape(24.dp)
)

@Composable
fun NammaHomeStayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = GlobalShapes,
        content = content
    )
}
