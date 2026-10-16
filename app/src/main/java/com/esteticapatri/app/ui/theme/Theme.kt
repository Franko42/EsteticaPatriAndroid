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

private val LightColorScheme = lightColorScheme(
    primary = RosePrimaryLight,
    onPrimary = RoseOnPrimaryLight,
    primaryContainer = RosePrimaryContainerLight,
    onPrimaryContainer = RoseOnPrimaryContainerLight,
    secondary = RoseSecondaryLight,
    onSecondary = RoseOnSecondaryLight,
    secondaryContainer = RoseSecondaryContainerLight,
    onSecondaryContainer = RoseOnSecondaryContainerLight,
    tertiary = RoseTertiaryLight,
    onTertiary = RoseOnTertiaryLight,
    tertiaryContainer = RoseTertiaryContainerLight,
    onTertiaryContainer = RoseOnTertiaryContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = RoseOnPrimaryDark,
    primaryContainer = RosePrimaryContainerDark,
    onPrimaryContainer = RoseOnPrimaryContainerDark,
    secondary = RoseSecondaryDark,
    onSecondary = RoseOnSecondaryDark,
    secondaryContainer = RoseSecondaryContainerDark,
    onSecondaryContainer = RoseOnSecondaryContainerDark,
    tertiary = RoseTertiaryDark,
    onTertiary = RoseOnTertiaryDark,
    tertiaryContainer = RoseTertiaryContainerDark,
    onTertiaryContainer = RoseOnTertiaryContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

@Composable
fun EsteticaPatriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        content = content
    )
}

object StatusColors {
    @Composable
    fun scheduled(darkTheme: Boolean = isSystemInDarkTheme()): Color =
        if (darkTheme) StatusScheduledDark else StatusScheduledLight

    @Composable
    fun completed(darkTheme: Boolean = isSystemInDarkTheme()): Color =
        if (darkTheme) StatusCompletedDark else StatusCompletedLight

    @Composable
    fun cancelled(darkTheme: Boolean = isSystemInDarkTheme()): Color =
        if (darkTheme) StatusCancelledDark else StatusCancelledLight

    @Composable
    fun noShow(darkTheme: Boolean = isSystemInDarkTheme()): Color =
        if (darkTheme) StatusNoShowDark else StatusNoShowLight
}
