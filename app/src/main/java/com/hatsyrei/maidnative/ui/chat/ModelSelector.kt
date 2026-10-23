package com.hatsyrei.maidnative.ui.chat

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.ui.common.AppDialog
import com.hatsyrei.maidnative.domain.modelLabels

@Composable
internal fun ModelSelector(
    models: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    tags: Boolean,
    onTagsChange: (Boolean) -> Unit,
) {
    if (models.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    Surface(
        onClick = { open = true },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            if (tags && selected.isNotEmpty()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.widthIn(max = 240.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) { modelLabels(selected).forEach { ModelLabel(it) } }
                }
            } else Text(
                text = selected.ifEmpty { tr("Select a model") },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                modifier = Modifier.widthIn(min = 88.dp, max = 240.dp).basicMarquee(),
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
    }
    if (open) {
        var query by rememberSaveable { mutableStateOf("") }
        val filtered = remember(models, query) {
            models.filter { it.contains(query.trim(), ignoreCase = true) }
        }
        AppDialog(
            onDismissRequest = { open = false },
            widthLimit = 840.dp,
            title = { Text(tr("Select model")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(tr("Search models ({0})" , models.size)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !tags, onClick = { onTagsChange(false) }, label = { Text(tr("List")) })
                        FilterChip(selected = tags, onClick = { onTagsChange(true) }, label = { Text(tr("Tags")) })
                    }
                    if (filtered.isEmpty()) Text(tr("No matching models"))
                    val select: (String) -> Unit = { model -> onSelect(model); open = false }
                    filtered.forEach { model ->
                        Surface(
                            onClick = { select(model) },
                            color = if (model == selected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            // Wrap the full identifier instead of hiding its distinguishing suffix.
                            if (tags) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    FlowRow(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) { modelLabels(model).forEach { ModelLabel(it) } }
                                }
                            } else Text(model, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text(tr("Close")) } },
        )
    }
}

@Composable
private fun ModelLabel(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
    }
}
