package com.hatsyrei.maidnative.ui.settings

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.domain.Sampling
import com.hatsyrei.maidnative.domain.SamplingParam
import com.hatsyrei.maidnative.ui.common.AppDialog
import com.hatsyrei.maidnative.ui.icons.CloseIcon
import com.hatsyrei.maidnative.ui.icons.RepeatIcon
import com.hatsyrei.maidnative.ui.icons.TuneIcon

internal enum class SamplingGroup(
    val title: String,
    val icon: ImageVector,
    val params: List<SamplingParam>,
) {
    SAMPLING(
        "Sampling",
        TuneIcon,
        listOf(SamplingParam.TEMPERATURE, SamplingParam.TOP_P, SamplingParam.TOP_K, SamplingParam.MIN_P),
    ),
    PENALTIES(
        "Penalty",
        RepeatIcon,
        listOf(SamplingParam.FREQUENCY_PENALTY, SamplingParam.PRESENCE_PENALTY),
    ),
}

/** Opens [group]'s dialog, and carries how many of its fields are currently overridden. */
@Composable
internal fun SamplingChip(group: SamplingGroup, sampling: Sampling, onClick: () -> Unit) {
    val overrides = group.params.count { sampling[it] != null }
    val active = overrides > 0
    AssistChip(
        onClick = onClick,
        label = { Text(if (active) "${tr(group.title)} · $overrides" else tr(group.title)) },
        leadingIcon = { Icon(group.icon, contentDescription = null) },
        // Filled when something is overridden, so the screen says at a glance
        // whether requests carry anything beyond the endpoint's own settings.
        // Same fill as the model pill and the selected chat entry.
        colors = if (!active) {
            AssistChipDefaults.assistChipColors()
        } else {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        border = if (active) null else AssistChipDefaults.assistChipBorder(enabled = true),
    )
}

/**
 * Per-field sampling overrides. Each row is off by default: an off field is left
 * out of the request entirely, so the endpoint's own flags — and the recommended
 * sampling llama.cpp reads from the model's metadata — still apply.
 */
@Composable
internal fun SamplingDialog(
    group: SamplingGroup,
    sampling: Sampling,
    onDismiss: () -> Unit,
    onConfirm: (Sampling) -> Unit,
) {
    var draft by remember { mutableStateOf(sampling) }

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr(group.title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                group.params.forEach { param ->
                    SamplingRow(param, draft[param]) { draft = draft.with(param, it) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }) { Text(tr("Save")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Cancel")) }
        },
    )
}

/** [value] null means the field is not overridden; [onChange] null turns it back off. */
@Composable
private fun SamplingRow(
    param: SamplingParam,
    value: Float?,
    onChange: (Float?) -> Unit,
) {
    val enabled = value != null
    // Holds the position while the row is off, so turning it back on resumes
    // where the user left it rather than snapping to the fallback.
    var parked by remember { mutableFloatStateOf(value ?: param.fallback) }
    val current = value ?: parked

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .toggleable(
                    value = enabled,
                    role = Role.Switch,
                    onValueChange = { on -> onChange(if (on) parked else null) },
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tr(param.label), style = MaterialTheme.typography.bodyLarge)
                Text(
                    tr(param.summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ValuePill(text = if (enabled) param.format(current) else tr("Auto"), active = enabled)
        }

        AnimatedVisibility(
            visible = enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            SlimSlider(
                value = current,
                onValueChange = {
                    parked = it
                    onChange(it)
                },
                valueRange = param.min..param.max,
            )
        }
    }
}

/** "Auto" in an outline while the endpoint decides; the user's value, filled, once overridden. */
@Composable
private fun ValuePill(text: String, active: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (active) colors.primaryContainer else Color.Transparent,
        contentColor = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
        border = if (active) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 72.dp)
                .padding(start = 12.dp, end = if (active) 8.dp else 12.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge)
            if (active) {
                Icon(CloseIcon, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** A thin track and round thumb, in place of M3's tall bar thumb and gapped track. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlimSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    val colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = colors,
        thumb = {
            Box(
                Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        },
        track = { state ->
            SliderDefaults.Track(
                sliderState = state,
                colors = colors,
                thumbTrackGapSize = 0.dp,
                drawStopIndicator = null,
                modifier = Modifier.height(4.dp),
            )
        },
    )
}
