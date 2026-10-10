package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.data.AppThemeMode

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = MinimalDarkPrimary,
    onPrimary = MinimalDarkOnPrimary,
    primaryContainer = MinimalDarkPrimaryContainer,
    onPrimaryContainer = MinimalDarkOnPrimaryContainer,
    background = MinimalDarkBackground,
    onBackground = MinimalDarkOnBackground,
    surface = MinimalDarkSurface,
    onSurface = MinimalDarkOnSurface,
    surfaceVariant = MinimalDarkSurfaceVariant,
    onSurfaceVariant = MinimalDarkOnSurfaceVariant,
    outline = MinimalDarkBorder,
    outlineVariant = MinimalDarkBorder
)

private val MinimalistLightColorScheme = lightColorScheme(
    primary = MinimalLightPrimary,
    onPrimary = MinimalLightOnPrimary,
    primaryContainer = MinimalLightPrimaryContainer,
    onPrimaryContainer = MinimalLightOnPrimaryContainer,
    background = MinimalLightBackground,
    onBackground = MinimalLightOnBackground,
    surface = MinimalLightSurface,
    onSurface = MinimalLightOnSurface,
    surfaceVariant = MinimalLightSurfaceVariant,
    onSurfaceVariant = MinimalLightOnSurfaceVariant,
    outline = MinimalLightBorder,
    outlineVariant = MinimalLightBorder
)

private val AmoledDarkColorScheme = darkColorScheme(
    primary = MinimalDarkPrimary,
    onPrimary = MinimalDarkOnPrimary,
    primaryContainer = MinimalDarkPrimaryContainer,
    onPrimaryContainer = MinimalDarkOnPrimaryContainer,
    background = androidx.compose.ui.graphics.Color(0xFF000000),
    onBackground = MinimalDarkOnBackground,
    surface = androidx.compose.ui.graphics.Color(0xFF000000),
    onSurface = MinimalDarkOnSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF101216),
    onSurfaceVariant = MinimalDarkOnSurfaceVariant,
    outline = androidx.compose.ui.graphics.Color(0xFF222630),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF181B22)
)

@Composable
fun DeviceTestTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    isAmoled: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        darkTheme && isAmoled -> AmoledDarkColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MinimalistDarkColorScheme
        else -> MinimalistLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
