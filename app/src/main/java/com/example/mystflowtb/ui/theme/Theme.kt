package com.example.mystflowtb.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definim paleta de culori bazată pe identitatea MystFlow TB
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4A77D),      // Rose Gold
    onPrimary = Color(0xFF00382B),    // Text închis pe butoane Rose Gold
    primaryContainer = Color(0xFF005240),
    onPrimaryContainer = Color(0xFFD4A77D),

    secondary = Color(0xFFD4A77D),
    onSecondary = Color(0xFF00382B),

    background = Color(0xFF00382B),   // Emerald Deep
    onBackground = Color.White,       // Text alb pe fundal verde

    surface = Color(0xFF00382B),      // Carduri/Surface-uri verzi
    onSurface = Color.White,

    outline = Color(0xFFD4A77D)       // Borduri Rose Gold
)

// Pentru o aplicație bancară premium, adesea forțăm tema închisă (Emerald)
// chiar și în modul Light pentru consistență, dar iată varianta Light:
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD4A77D),
    onPrimary = Color(0xFF00382B),
    background = Color(0xFF00382B),
    surface = Color(0xFF00382B),
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0xFFD4A77D)
)

@Composable
fun MystFlowTBTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color este disponibil pe Android 12+, dar îl dezactivăm
    // pentru a păstra branding-ul strict Emerald & Rose Gold
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Setăm bara de sus (StatusBar) să fie tot Emerald Deep
            window.statusBarColor = Color(0xFF00382B).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asigură-te că fișierul Typography.kt există
        content = content
    )
}