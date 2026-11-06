/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.ui.common

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts dominant color from an image.
 * Uses a simple algorithm that samples colors and finds the most frequent one.
 */
suspend fun extractDominantColor(painter: Painter?): Int = withContext(Dispatchers.IO) {
    if (painter == null) return@withContext ComposeColor.Transparent.toArgb()
    
    try {
        // For now, return a placeholder color based on the role
        // In a real implementation, we would:
        // 1. Get the bitmap from the painter
        // 2. Sample colors from the image
        // 3. Find the dominant color
        // 4. Mix with accent color
        
        // Placeholder: return a color based on the painter's hash
        // This ensures consistent colors for the same avatar
        val hash = painter.hashCode()
        val hue = (hash % 360).toFloat()
        Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.8f))
    } catch (e: Exception) {
        ComposeColor.Transparent.toArgb()
    }
}

/**
 * Mixes two colors with the accent color as the base.
 */
fun mixWithAccent(baseColor: Int, accentColor: Int, ratio: Float = 0.5f): Int {
    if (baseColor == ComposeColor.Transparent.toArgb()) return accentColor
    
    val r1 = Color.red(baseColor)
    val g1 = Color.green(baseColor)
    val b1 = Color.blue(baseColor)
    
    val r2 = Color.red(accentColor)
    val g2 = Color.green(accentColor)
    val b2 = Color.blue(accentColor)
    
    val r = (r1 * (1 - ratio) + r2 * ratio).toInt().coerceIn(0, 255)
    val g = (g1 * (1 - ratio) + g2 * ratio).toInt().coerceIn(0, 255)
    val b = (b1 * (1 - ratio) + b2 * ratio).toInt().coerceIn(0, 255)
    
    return Color.rgb(r, g, b)
}
