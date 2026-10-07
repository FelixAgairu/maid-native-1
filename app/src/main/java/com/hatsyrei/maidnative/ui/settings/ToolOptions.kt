package com.hatsyrei.maidnative.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.domain.tools.FetchUrl
import com.hatsyrei.maidnative.domain.tools.FileTools
import com.hatsyrei.maidnative.domain.tools.GetDatetime
import com.hatsyrei.maidnative.domain.tools.RollDice
import com.hatsyrei.maidnative.domain.tools.RunJavaScript
import com.hatsyrei.maidnative.domain.tools.ToolEffect
import com.hatsyrei.maidnative.domain.tools.ToolSwitch
import com.hatsyrei.maidnative.domain.tools.Tools
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
        label = { Text(if (active) "Tools · $count" else "Tools") },
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
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    // Names of tools this build no longer has are dropped on save.
    var draft by remember { mutableStateOf(enabled.filter { name -> Tools.switches.any { it.name == name } }.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = { Text("Tools") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Tools.switches.forEach { tool ->
                    ToolRow(tool, checked = tool.name in draft) { on ->
                        draft = if (on) draft + tool.name else draft - tool.name
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun ToolRow(tool: ToolSwitch, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onToggle),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            toolIcon(tool),
            contentDescription = null,
            tint = if (checked) colors.primary else colors.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tool.label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                tool.effects.forEach { EffectTag(it) }
            }
            Text(
                tool.summary,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // The row handles the toggle, so the checkbox only shows state.
        Checkbox(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun EffectTag(effect: ToolEffect) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            effect.label,
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
