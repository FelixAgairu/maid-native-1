/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.hatsyrei.maidnative.data.prefs.SettingsRepository
import com.hatsyrei.maidnative.ui.markdown.ProvideChatMarkdownStyle

// The RN app is hardcoded dark (useTheme), so this is dark-only: no light
// scheme, no dynamic color.
//
// EVERY role is set explicitly. Anything left to `darkColorScheme()`'s defaults
// falls back to Material's baseline dark palette, whose neutrals are
// purple-tinted (surfaceContainerLow #1D1B20, surfaceContainer #211F26,
// surfaceContainerHigh #2B2930) and visibly clash with the blue seed. The
// replacements are a black-anchored blue-neutral ramp - see Color.kt.
private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    // Tonal elevation only ever applies to `surface` itself, and lifting the
    // AMOLED canvas off black is exactly what we don't want, so the tint is a
    // no-op by construction. Explicit containers stay at their exact tone.
    surfaceTint = Color.Transparent,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    primaryFixed = DarkPrimaryFixed,
    primaryFixedDim = DarkPrimaryFixedDim,
    onPrimaryFixed = DarkOnPrimaryFixed,
    onPrimaryFixedVariant = DarkOnPrimaryFixedVariant,
    secondaryFixed = DarkSecondaryFixed,
    secondaryFixedDim = DarkSecondaryFixedDim,
    onSecondaryFixed = DarkOnSecondaryFixed,
    onSecondaryFixedVariant = DarkOnSecondaryFixedVariant,
    tertiaryFixed = DarkTertiaryFixed,
    tertiaryFixedDim = DarkTertiaryFixedDim,
    onTertiaryFixed = DarkOnTertiaryFixed,
    onTertiaryFixedVariant = DarkOnTertiaryFixedVariant,
)

@Composable
fun MaidNativeTheme(
    theme: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val colors = remember(theme.accentColor, theme.backgroundColor) {
        val base = if (theme.accentColor == 0) DarkColors else DarkColors.withAccent(Color(theme.accentColor))
        base.copy(background = Color(theme.backgroundColor))
    }
    val typography = remember(theme.fontScale) {
        scaledTypographyInternal(theme.fontScale)
    }
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
    ) {
        // Apply in-app UI scale by adjusting density (scales all dp-based measurements)
        val density = LocalDensity.current
        val scaledDensity = remember(density, theme.fontScale) {
            Density(density = density.density * theme.fontScale, fontScale = density.fontScale)
        }
        androidx.compose.runtime.CompositionLocalProvider(LocalDensity provides scaledDensity) {
            // Built here (above every state-reading composable) so the markdown
            // typography/padding/annotator are allocated once per activity instead
            // of once per bubble per recomposition.
            ProvideChatMarkdownStyle(content)
        }
    }
}

/**
 * Creates a typography with [factor] applied to all text sizes.
 * When [factor] is 1f, the original typography is returned unchanged.
 */
@Composable
private fun scaledTypography(factor: Float): Typography = scaledTypographyInternal(factor)

private fun scaledTypographyInternal(factor: Float): Typography {
    if (factor == 1f) return Typography
    fun TextStyle.scaled(): TextStyle = copy(fontSize = fontSize * factor)
    return Typography.copy(
        displayLarge = Typography.displayLarge.scaled(),
        displayMedium = Typography.displayMedium.scaled(),
        displaySmall = Typography.displaySmall.scaled(),
        headlineLarge = Typography.headlineLarge.scaled(),
        headlineMedium = Typography.headlineMedium.scaled(),
        headlineSmall = Typography.headlineSmall.scaled(),
        titleLarge = Typography.titleLarge.scaled(),
        titleMedium = Typography.titleMedium.scaled(),
        titleSmall = Typography.titleSmall.scaled(),
        bodyLarge = Typography.bodyLarge.scaled(),
        bodyMedium = Typography.bodyMedium.scaled(),
        bodySmall = Typography.bodySmall.scaled(),
        labelLarge = Typography.labelLarge.scaled(),
        labelMedium = Typography.labelMedium.scaled(),
        labelSmall = Typography.labelSmall.scaled(),
    )
}

private fun scaledTypography(base: Typography, factor: Float): Typography {
    if (factor == 1f) return base
    fun TextStyle.scaled(): TextStyle = copy(fontSize = fontSize * factor)
    return base.copy(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled(),
    )
}

/** The slice of settings the theme depends on, kept separate so streaming tokens can't recompose it. */
@Immutable
data class ThemeSettings(
    val accentColor: Int = 0,
    val backgroundColor: Int = 0,
    val fontScale: Float = 1f,
    /**
     * Pinned to none independently of [SettingsRepository.DEFAULT_NAMEPLATE]:
     * this instance is what the theme sees before DataStore's first read lands,
     * and painting any art there would flash it on every launch for anyone who
     * chose something else.
     */
    val nameplate: String = SettingsRepository.NAMEPLATE_NONE,
    val nameplateStamp: Long = 0L,
)
