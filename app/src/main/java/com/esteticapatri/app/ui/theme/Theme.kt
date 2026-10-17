package com.esteticapatri.app.ui.theme

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

private val PastelLightColorScheme = lightColorScheme(
    primary = PastelRose,
    onPrimary = Color(0xFF111111), // Negro profundo para máximo contraste sobre rosa claro
    primaryContainer = PastelRoseLightContainer,
    onPrimaryContainer = PastelRoseOnContainer,
    secondary = PastelLavender,
    onSecondary = Color(0xFF111111),
    secondaryContainer = PastelLavenderContainer,
    onSecondaryContainer = PastelLavenderOnContainer,
    tertiary = PastelPeach,
    onTertiary = Color(0xFF111111),
    tertiaryContainer = PastelPeachContainer,
    onTertiaryContainer = PastelPeachOnContainer,
    background = PastelBackground,
    onBackground = PastelOnBackground,
    surface = PastelSurface,
    onSurface = PastelOnSurface,
    surfaceVariant = PastelSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A3B40),
    outline = PastelOutline
)

private val VelvetDarkColorScheme = darkColorScheme(
    primary = VelvetRose,
    onPrimary = Color(0xFF111111),
    primaryContainer = VelvetRoseContainer,
    onPrimaryContainer = VelvetRoseOnContainer,
    secondary = VelvetLavender,
    onSecondary = Color(0xFF111111),
    secondaryContainer = VelvetLavenderContainer,
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = Color(0xFFFFB69D),
    onTertiary = Color(0xFF111111),
    background = VelvetBackground,
    onBackground = VelvetOnBackground,
    surface = VelvetSurface,
    onSurface = VelvetOnSurface,
    surfaceVariant = VelvetSurfaceVariant,
    onSurfaceVariant = Color(0xFFE8CCD2),
    outline = VelvetOutline
)

@Composable
fun EsteticaPatriTheme(
    darkTheme: Boolean = false, // Forzado modo claro por defecto
    dynamicColor: Boolean = false, // Mantener identidad estética de salón
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> VelvetDarkColorScheme
        else -> PastelLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

object StatusColors {
    data class StatusToken(val color: Color, val container: Color)

    @Composable
    fun scheduled(darkTheme: Boolean = isSystemInDarkTheme()): StatusToken =
        if (darkTheme) StatusToken(Color(0xFF93C5FD), Color(0xFF1E3A8A))
        else StatusToken(StatusScheduledPastel, StatusScheduledContainer)

    @Composable
    fun completed(darkTheme: Boolean = isSystemInDarkTheme()): StatusToken =
        if (darkTheme) StatusToken(Color(0xFF6EE7B7), Color(0xFF064E3B))
        else StatusToken(StatusCompletedPastel, StatusCompletedContainer)

    @Composable
    fun cancelled(darkTheme: Boolean = isSystemInDarkTheme()): StatusToken =
        if (darkTheme) StatusToken(Color(0xFFFCA5A5), Color(0xFF7F1D1D))
        else StatusToken(StatusCancelledPastel, StatusCancelledContainer)

    @Composable
    fun noShow(darkTheme: Boolean = isSystemInDarkTheme()): StatusToken =
        if (darkTheme) StatusToken(Color(0xFFFCD34D), Color(0xFF78350F))
        else StatusToken(StatusNoShowPastel, StatusNoShowContainer)
}
