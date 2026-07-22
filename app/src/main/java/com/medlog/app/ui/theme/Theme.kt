package com.medlog.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 theme for MedLog.
 * Uses the teal/emerald health palette defined in Color.kt.
 * Supports dynamic colors on Android 12+ (Material You), falling
 * back to the custom palette on older devices.
 */
@Composable
fun MedLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = md_theme_primary,
            onPrimary = md_theme_on_primary,
            primaryContainer = md_theme_primary_container,
            onPrimaryContainer = md_theme_on_primary_container,
            secondary = md_theme_secondary,
            onSecondary = md_theme_on_secondary,
            secondaryContainer = md_theme_secondary_container,
            onSecondaryContainer = md_theme_on_secondary_container,
            tertiary = md_theme_tertiary,
            onTertiary = md_theme_on_tertiary,
            tertiaryContainer = md_theme_tertiary_container,
            onTertiaryContainer = md_theme_on_tertiary_container,
            error = md_theme_error,
            onError = md_theme_on_error,
            errorContainer = md_theme_error_container,
            onErrorContainer = md_theme_on_error_container,
            background = Color(0xFF191C1B),
            onBackground = Color(0xFFE1E3E1),
            surface = Color(0xFF191C1B),
            onSurface = Color(0xFFE1E3E1),
            surfaceVariant = Color(0xFF3F4946),
            onSurfaceVariant = Color(0xFFBFC9C5),
            outline = Color(0xFF89938F),
            outlineVariant = Color(0xFF3F4946)
        )
        else -> lightColorScheme(
            primary = md_theme_primary,
            onPrimary = md_theme_on_primary,
            primaryContainer = md_theme_primary_container,
            onPrimaryContainer = md_theme_on_primary_container,
            secondary = md_theme_secondary,
            onSecondary = md_theme_on_secondary,
            secondaryContainer = md_theme_secondary_container,
            onSecondaryContainer = md_theme_on_secondary_container,
            tertiary = md_theme_tertiary,
            onTertiary = md_theme_on_tertiary,
            tertiaryContainer = md_theme_tertiary_container,
            onTertiaryContainer = md_theme_on_tertiary_container,
            error = md_theme_error,
            onError = md_theme_on_error,
            errorContainer = md_theme_error_container,
            onErrorContainer = md_theme_on_error_container,
            background = md_theme_background,
            onBackground = md_theme_on_background,
            surface = md_theme_surface,
            onSurface = md_theme_on_surface,
            surfaceVariant = md_theme_surface_variant,
            onSurfaceVariant = md_theme_on_surface_variant,
            outline = md_theme_outline,
            outlineVariant = md_theme_outline_variant
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MedLogTypography,
        content = content
    )
}