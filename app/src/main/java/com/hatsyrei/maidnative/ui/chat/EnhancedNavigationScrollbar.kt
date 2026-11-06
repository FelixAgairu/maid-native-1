/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.ui.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hatsyrei.maidnative.domain.tree.MessageNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Enhanced scrollbar with navigation bookmarks and preview popups.
 * Shows 3-5 words of user prompt and 7-9 words of agent response in popups.
 */
@Composable
fun EnhancedNavigationScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    messages: List<MessageNode>,
    trailingSpacerHeight: Dp = 0.dp,
    resetKey: Any? = null,
    alwaysShowScrollbar: Boolean = false,
) {
    val density = LocalDensity.current
    val thumbHeightPx = with(density) { 64.dp.toPx() }
    val spacingPx = with(density) { 12.dp.toPx() }
    val trailingSpacerPx = with(density) { trailingSpacerHeight.toPx() }
    val sizeCache = remember(listState, resetKey) { ItemSizeCache() }

    val metrics = remember(listState, sizeCache, thumbHeightPx, spacingPx, trailingSpacerPx) {
        derivedStateOf {
            computeMetrics(listState, thumbHeightPx, spacingPx, trailingSpacerPx, sizeCache)
        }
    }
    val visible by remember(metrics, alwaysShowScrollbar) {
        derivedStateOf { alwaysShowScrollbar || metrics.value.visible }
    }

    val opacity = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf<MessageNode?>(null) }
    var popupPosition by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState, opacity) {
        combine(
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset },
            snapshotFlow { dragging },
            snapshotFlow { metrics.value.visible },
        ) { _, isDragging, isVisible -> isDragging to isVisible }
            .collectLatest { (isDragging, isVisible) ->
                if (!isVisible) {
                    opacity.snapTo(0f)
                    return@collectLatest
                }
                opacity.snapTo(1f)
                if (!isDragging) {
                    delay(1200)
                    opacity.animateTo(0f, tween(300))
                }
            }
    }

    if (!visible) return

    val interactive by remember(opacity) {
        derivedStateOf { dragging || opacity.value > 0.05f }
    }

    Box(modifier = modifier.fillMaxHeight().width(22.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, metrics.value.thumbTopPx.roundToInt()) }
                .height(64.dp)
                .width(22.dp)
                .graphicsLayer { alpha = opacity.value }
                .then(
                    if (interactive) {
                        Modifier.pointerInput(listState, metrics) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val initialY = down.position.y
                                val initialThumbTop = metrics.value.thumbTopPx
                                var claimed = false
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    val dy = change.position.y - initialY
                                    // Use a larger threshold than the LazyColumn's touch slop
                                    // so quick scrolls are not stolen by the scrollbar.
                                    if (!claimed && kotlin.math.abs(dy) > 24f) {
                                        claimed = true
                                        dragging = true
                                    }
                                    if (claimed) {
                                        change.consume()
                                        val maxScroll = metrics.value.maxScrollPx
                                        val travel = metrics.value.thumbTravelPx
                                        if (maxScroll > 0f && travel > 0f) {
                                            val newThumbTop = (initialThumbTop + dy).coerceIn(0f, travel)
                                            val scrollPercent = newThumbTop / travel
                                            val scrollTo = (scrollPercent * maxScroll).roundToInt()
                                            coroutineScope.launch {
                                                listState.scrollToItem(scrollTo.toInt())
                                            }
                                        }
                                    }
                                }
                                if (claimed) dragging = false
                            }
                        }
                    } else Modifier
                )
                .background(
                    if (dragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp),
                )
                .clip(RoundedCornerShape(4.dp))
                .pointerInput(interactive) {
                    detectTapGestures {
                        val tapPercent = it.y / metrics.value.thumbTravelPx
                        val scrollTo = (tapPercent * metrics.value.maxScrollPx).roundToInt()
                        coroutineScope.launch {
                            listState.scrollToItem(scrollTo.toInt())
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            // Show bookmark indicators
            messages.forEachIndexed { index, message ->
                if (message.role != "system" && index > 0 && index < messages.size - 1) {
                    val itemTop = remember(listState, index) {
                        derivedStateOf {
                            var top = 0f
                            for (i in 0 until index) {
                                top += sizeCache.sizeAt(i).toFloat() + spacingPx
                            }
                            top
                        }
                    }.value

                    val thumbTop = metrics.value.thumbTopPx
                    val thumbBottom = thumbTop + thumbHeightPx
                    val itemRelativeTop = itemTop - listState.firstVisibleItemScrollOffset
                    val itemRelativeBottom = itemRelativeTop + sizeCache.sizeAt(index)

                    if (itemRelativeTop < thumbBottom && itemRelativeBottom > thumbTop) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset { IntOffset(0, ((itemRelativeTop - thumbTop) / (thumbBottom - thumbTop) * thumbHeightPx).roundToInt()) }
                                .size(6.dp)
                                .background(
                                    if (message.role == "user") MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.tertiary,
                                    CircleShape
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        coroutineScope.launch {
                                            listState.scrollToItem(index)
                                        }
                                        // Show popup with preview
                                        popupMessage = message
                                        popupPosition = itemRelativeTop
                                        showPopup = true
                                    }
                                },
                        )
                    }
                }
            }
        }

        // Popup preview
        if (showPopup && popupMessage != null) {
            Popup(
                alignment = Alignment.BottomEnd,
                offset = IntOffset(0, popupPosition.roundToInt()),
                properties = PopupProperties(focusable = false),
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .widthIn(max = 200.dp)
                    ) {
                        val previewText = when (popupMessage?.role) {
                            "user" -> popupMessage?.content?.take(15)?.trim() ?: ""
                            "assistant" -> popupMessage?.content?.take(45)?.trim() ?: ""
                            else -> ""
                        }
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
