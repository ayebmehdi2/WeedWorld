package com.itshedi.weedworld.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

//private val DarkColorPalette = darkColors(
//    primary = accentGreen,
//    primaryVariant = Purple700,
//    secondary = Teal200
//)

private val LightColorPalette = lightColors(
    primary = accentGreen,
    primaryVariant = accentGreen,
    secondary = accentGreen

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun WeedWorldTheme(/*darkTheme: Boolean = isSystemInDarkTheme(),*/ content: @Composable () -> Unit) {
//    val colors = if (darkTheme) {
//        DarkColorPalette
//    } else {
//        LightColorPalette
//    }
    val colors = LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}