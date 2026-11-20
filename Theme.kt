package br.com.projeto180dias.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Forest, onPrimary = Color.White,
    secondary = Copper, onSecondary = Color.White,
    background = Cream, onBackground = Ink,
    surface = SurfaceWhite, onSurface = Ink,
    surfaceVariant = Sage, onSurfaceVariant = Muted,
    outline = Line
)

@Composable
fun Projeto180DiasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = androidx.compose.material3.Typography(), content = content)
}
