package com.hatsyrei.maidnative.ui.settings

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.domain.tools.FetchUrl
import com.hatsyrei.maidnative.domain.tools.FileTools
import com.hatsyrei.maidnative.domain.tools.GetDatetime
import com.hatsyrei.maidnative.domain.tools.RollDice
import com.hatsyrei.maidnative.domain.tools.RunJavaScript
import com.hatsyrei.maidnative.domain.tools.ToolEffect
import com.hatsyrei.maidnative.domain.tools.ToolPolicy
import com.hatsyrei.maidnative.domain.tools.ToolSwitch
import com.hatsyrei.maidnative.domain.tools.Tools
import com.hatsyrei.maidnative.ui.common.AppDialog
import com.hatsyrei.maidnative.ui.icons.CasinoIcon
import com.hatsyrei.maidnative.ui.icons.CodeIcon
import com.hatsyrei.maidnative.ui.icons.DescriptionIcon
import com.hatsyrei.maidnative.ui.icons.LinkIcon
import com.hatsyrei.maidnative.ui.icons.ScheduleIcon

/** Opens the tools dialog, and carries how many tools are currently enabled. */
@Composable
internal fun ToolsChip(enabled: Set<String>, onClick: () -> Unit) {
    val count = Tools.switches.count { it.name in enabled }
    val active = count > 0
    AssistChip(
        onClick = onClick,
        label = { Text(if (active) tr("Tools · {0}" , count) else tr("Tools")) },
        // The wrench fills its box more than the other chip glyphs, so it runs a size down.
        leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(16.dp)) },
        // Filled for the same reason as the sampling chip: requests carry more than the chat.
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

@Composable
internal fun ToolsDialog(
    enabled: Set<String>,
    ask: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, ToolPolicy>) -> Unit,
) {
    // Every switch this build has; names it no longer has are dropped on save.
    var draft by remember {
        mutableStateOf(
            Tools.switches.associate { tool ->
                tool.name to when (tool.name) {
                    !in enabled -> ToolPolicy.OFF
                    in ask -> ToolPolicy.ASK
                    else -> ToolPolicy.ALLOW
                }
            },
        )
    }

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Tools")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Tools.switches.forEach { tool ->
                    ToolRow(tool, policy = draft.getValue(tool.name)) { draft = draft + (tool.name to it) }
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

/** A tool with its effects and an Off / Ask / Allow choice under it; [askEnabled] false greys out Ask. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ToolRow(
    tool: ToolSwitch,
    policy: ToolPolicy,
    askEnabled: Boolean = true,
    onPolicy: (ToolPolicy) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            toolIcon(tool),
            contentDescription = null,
            tint = if (policy != ToolPolicy.OFF) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tr(tool.label),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                tool.effects.forEach { EffectTag(it) }
            }
            Text(
                tr(tool.summary),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // M3 pads each segment out to a 48dp touch target, which nearly doubled the row.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                ) {
                    ToolPolicy.entries.forEachIndexed { index, choice ->
                        SegmentedButton(
                            selected = policy == choice,
                            onClick = { onPolicy(choice) },
                            enabled = choice != ToolPolicy.ASK || askEnabled,
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ToolPolicy.entries.size),
                            // The same fill as the scan dialog's subnet choice.
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = colors.surfaceVariant,
                                activeContentColor = colors.onSurfaceVariant,
                            ),
                            icon = {},
                            label = { Text(tr(choice.label), style = MaterialTheme.typography.labelMedium) },
                            modifier = Modifier.height(32.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectTag(effect: ToolEffect) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            tr(effect.label),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
        )
    }
}

private fun toolIcon(tool: ToolSwitch): ImageVector = when (tool) {
    GetDatetime -> ScheduleIcon
    RollDice -> CasinoIcon
    FetchUrl -> LinkIcon
    RunJavaScript -> CodeIcon
    FileTools -> DescriptionIcon
    else -> Icons.Filled.Build
}
