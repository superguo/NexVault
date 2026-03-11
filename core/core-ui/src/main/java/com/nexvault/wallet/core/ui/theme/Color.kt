package com.nexvault.wallet.core.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== Dark Theme (Primary) ====================

// Background & Surface
val DarkBackground = Color(0xFF0A0E1A)
val DarkSurface = Color(0xFF121829)
val DarkSurfaceVariant = Color(0xFF1A1F35)
val DarkSurfaceVariant2 = Color(0xFF232842)

// Primary colors
val DarkPrimary = Color(0xFF2D5AF0)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkPrimaryContainer = Color(0xFF1E3A8A)
val DarkOnPrimaryContainer = Color(0xFFD8E2FF)

// Secondary colors
val DarkSecondary = Color(0xFF00D4AA)
val DarkOnSecondary = Color(0xFF003730)
val DarkSecondaryContainer = Color(0xFF005045)
val DarkOnSecondaryContainer = Color(0xFF70F7DC)

// Tertiary colors
val DarkTertiary = Color(0xFF7B61FF)
val DarkOnTertiary = Color(0xFF2E0080)
val DarkTertiaryContainer = Color(0xFF4A00B8)
val DarkOnTertiaryContainer = Color(0xFFE8DDFF)

// Error colors
val DarkError = Color(0xFFFF4C6E)
val DarkOnError = Color(0xFFFFFFFF)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// On colors (high/medium/disabled emphasis)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFA0A8C8)
val DarkOnSurfaceDisabled = Color(0xFF5A6180)

// Outline & borders
val DarkOutline = Color(0xFF404759)
val DarkOutlineVariant = Color(0xFF252B3F)

// Inverse colors
val DarkInverseSurface = Color(0xFFE6E1E5)
val DarkInverseOnSurface = Color(0xFF313033)
val DarkInversePrimary = Color(0xFF0A2472)

// ==================== Light Theme ====================

// Background & Surface
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F6FA)
val LightSurfaceVariant = Color(0xFFE7E8EF)
val LightSurfaceVariant2 = Color(0xFFD1D3DE)

// Primary colors
val LightPrimary = Color(0xFF2D5AF0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD8E2FF)
val LightOnPrimaryContainer = Color(0xFF001849)

// Secondary colors
val LightSecondary = Color(0xFF006B56)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFF70F7DC)
val LightOnSecondaryContainer = Color(0xFF00201A)

// Tertiary colors
val LightTertiary = Color(0xFF6B4DE1)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFE8DDFF)
val LightOnTertiaryContainer = Color(0xFF2300A0)

// Error colors
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// On colors
val LightOnBackground = Color(0xFF0A0E1A)
val LightOnSurface = Color(0xFF0A0E1A)
val LightOnSurfaceVariant = Color(0xFF5A6180)
val LightOnSurfaceDisabled = Color(0xFF9A9CA8)

// Outline & borders
val LightOutline = Color(0xFF74778C)
val LightOutlineVariant = Color(0xFFC4C6D4)

// Inverse colors
val LightInverseSurface = Color(0xFF2F3033)
val LightInverseOnSurface = Color(0xFFF1F0F4)
val LightInversePrimary = Color(0xFFB3C4FF)

// ==================== Material 3 Color Schemes ====================

val NexVaultDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    surfaceTint = DarkPrimary,
)

val NexVaultLightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceTint = LightPrimary,
)
