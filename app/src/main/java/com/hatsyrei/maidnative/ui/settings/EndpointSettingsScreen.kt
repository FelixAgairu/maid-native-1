/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.data.prefs.SettingsRepository
import com.hatsyrei.maidnative.ui.chat.ChatUiState
import com.hatsyrei.maidnative.ui.common.AppDialog
import com.hatsyrei.maidnative.ui.common.TextInputDialog
import com.hatsyrei.maidnative.ui.i18n.tr

@Composable
fun EndpointSettingsScreen(
    state: ChatUiState,
    presets: List<SettingsRepository.EndpointPreset>,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    var renaming by remember { mutableStateOf<SettingsRepository.EndpointPreset?>(null) }
    var editing by remember { mutableStateOf<SettingsRepository.EndpointPreset?>(null) }
    var deleting by remember { mutableStateOf<SettingsRepository.EndpointPreset?>(null) }
    var expandedMenu by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            tr("Saved endpoints"),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(16.dp))

        if (presets.isEmpty()) {
            Text(
                tr("No saved endpoints"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(presets, key = { it.id }) { preset ->
                    EndpointItem(
                        preset = preset,
                        isActive = preset.baseURL == state.settings.baseURL,
                        onApply = { actions.applyPreset(preset) },
                        onRename = { renaming = preset },
                        onEdit = { editing = preset },
                        onDelete = { deleting = preset },
                        expandedMenu = expandedMenu == preset.id,
                        onMenuClick = { expandedMenu = if (expandedMenu == preset.id) null else preset.id },
                        onDismissMenu = { expandedMenu = null },
                    )
                }
            }
        }
    }

    // Rename dialog
    renaming?.let { preset ->
        TextInputDialog(
            title = tr("Rename endpoint"),
            initial = preset.name,
            confirmLabel = tr("Rename"),
            label = tr("Name"),
            onDismiss = { renaming = null },
            onConfirm = { name ->
                renaming = null
                actions.renamePreset(preset.id, name)
            },
        )
    }

    // Edit dialog with URL and API key fields
    editing?.let { preset ->
        EndpointEditDialog(
            preset = preset,
            onDismiss = { editing = null },
            onSave = { url, apiKey ->
                editing = null
                actions.applyPreset(preset.copy(baseURL = url, apiKey = apiKey))
            },
        )
    }

    // Delete confirmation
    deleting?.let { preset ->
        TextInputDialog(
            title = tr("Delete endpoint"),
            initial = "",
            confirmLabel = tr("Delete"),
            label = tr("Confirm name"),
            onDismiss = { deleting = null },
            onConfirm = { name ->
                deleting = null
                if (name == preset.name) {
                    actions.deletePreset(preset.id)
                }
            },
        )
    }
}

@Composable
private fun EndpointEditDialog(
    preset: SettingsRepository.EndpointPreset,
    onDismiss: () -> Unit,
    onSave: (url: String, apiKey: String) -> Unit,
) {
    var url by remember { mutableStateOf(preset.baseURL) }
    var apiKey by remember { mutableStateOf(preset.apiKey) }

    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Edit endpoint")) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(tr("Base URL")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(tr("API key")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrectEnabled = false,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(url, apiKey) }) {
                Text(tr("Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancel"))
            }
        },
    )
}

@Composable
private fun EndpointItem(
    preset: SettingsRepository.EndpointPreset,
    isActive: Boolean,
    onApply: () -> Unit,
    onRename: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    expandedMenu: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    preset.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isActive) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                )
                if (isActive) {
                    Text(
                        tr("Active"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                preset.baseURL,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(onClick = onMenuClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.MoreVert, contentDescription = tr("Options"))
        }

        DropdownMenu(
            expanded = expandedMenu,
            onDismissRequest = onDismissMenu,
        ) {
            DropdownMenuItem(
                text = { Text(tr("Use")) },
                onClick = { onDismissMenu(); onApply() },
            )
            DropdownMenuItem(
                text = { Text(tr("Rename")) },
                onClick = { onDismissMenu(); onRename() },
            )
            DropdownMenuItem(
                text = { Text(tr("Edit")) },
                onClick = { onDismissMenu(); onEdit() },
            )
            DropdownMenuItem(
                text = { Text(tr("Delete")) },
                onClick = { onDismissMenu(); onDelete() },
            )
        }
    }
}