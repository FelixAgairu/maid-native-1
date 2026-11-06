/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.ui.settings

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.data.prefs.SettingsRepository.EndpointPreset
import com.hatsyrei.maidnative.domain.tools.McpServer
import com.hatsyrei.maidnative.ui.chat.ChatUiState
import com.hatsyrei.maidnative.ui.chat.ConfirmDialog
import com.hatsyrei.maidnative.ui.chat.ModelSelector
import com.hatsyrei.maidnative.ui.icons.BookmarksIcon
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: ChatUiState,
    presets: List<EndpointPreset>,
    mcpServers: List<McpServer>,
    actions: SettingsActions,
    onBack: () -> Unit,
) {
    val baseURL = rememberTextFieldState(state.settings.baseURL)
    val apiKey = rememberTextFieldState(state.settings.apiKey)
    var showPresets by remember { mutableStateOf(false) }
    var showScanOptions by remember { mutableStateOf(false) }
    var editingSampling by remember { mutableStateOf<SamplingGroup?>(null) }
    var showTools by remember { mutableStateOf(false) }
    var showMcp by remember { mutableStateOf(false) }
    var naming by remember { mutableStateOf<EndpointPreset?>(null) }
    var savingNew by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<EndpointPreset?>(null) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val pickCert = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { actions.setCustomCertificatePath(uri.toString()) }
    }

    // Reset the scan button to its idle state each time Settings is opened.
    LaunchedEffect(Unit) { actions.resetScan() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Settings")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back"))
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Shrinks the scroll viewport by the keyboard, so a focused
                // field near the bottom can be scrolled (and is auto-brought)
                // into view instead of sitting under the IME. `consume` stops
                // the nav-bar inset already in `padding` being counted twice.
                .consumeWindowInsets(padding)
                .imePadding()
                // A tap on empty space drops focus, which is what commits a
                // half-typed field. Runs after any child handles the tap.
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. CONNECTION & API SETTINGS
            Text(tr("Endpoint"), style = MaterialTheme.typography.titleMedium)
            TextButton(
                onClick = {
                    val intent = android.content.Intent(context, com.hatsyrei.maidnative.EndpointSettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(tr("Configure endpoint"))
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))

            // 2. MODEL & GENERATION SETTINGS
            Text(tr("Model"), style = MaterialTheme.typography.titleMedium)
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (state.models.isEmpty()) {
                Text(
                    tr("No models loaded. Tap the scan button or refresh to fetch /models."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ModelSelector(
                    models = state.models,
                    selected = state.settings.model,
                    onSelect = actions.setModel,
                    tags = state.settings.modelPickerTags,
                    onTagsChange = actions.setModelPickerTags,
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = actions.refreshModels,
                    enabled = !state.refreshingModels,
                    label = { Text(tr("Refresh models")) },
                    leadingIcon = {
                        if (state.refreshingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                        }
                    },
                )

                SamplingGroup.entries.forEach { group ->
                    SamplingChip(
                        group = group,
                        sampling = state.settings.sampling,
                        onClick = {
                            focusManager.clearFocus()
                            editingSampling = group
                        },
                    )
                }

                ToolsChip(
                    enabled = state.settings.enabledTools,
                    onClick = {
                        focusManager.clearFocus()
                        showTools = true
                    },
                )

                McpChip(
                    servers = mcpServers,
                    onClick = {
                        focusManager.clearFocus()
                        showMcp = true
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr("Thinking"), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        tr("Asks the endpoint to enable or disable the model's reasoning trace. Endpoints that don't support the request ignore it."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.settings.reasoning,
                    onCheckedChange = actions.setReasoning,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))

            // 3. CHAT EXPERIENCE SETTINGS
            PreferenceSwitch(
                title = tr("Automatically name new chats"),
                description = tr("Uses the first message locally; keeps manually chosen titles and uses no extra tokens."),
                checked = state.settings.autoRenameChats,
                onChange = actions.setAutoRenameChats,
            )

            PreferenceSwitch(
                title = tr("Scroll long messages"),
                description = tr("Long messages and reasoning scroll inside their cards. Expand any card to read it in full."),
                checked = state.settings.scrollLongMessages,
                onChange = actions.setScrollLongMessages,
            )

            PreferenceSwitch(
                title = tr("Always show scrollbar"),
                description = tr("Show scrollbar even when not scrolling."),
                checked = state.settings.alwaysShowScrollbar,
                onChange = actions.setAlwaysShowScrollbar,
            )

            PreferenceSwitch(
                title = tr("Auto-collapse messages"),
                description = tr("Automatically collapse long messages to show only first two lines."),
                checked = state.settings.collapseMessages,
                onChange = actions.setCollapseMessages,
            )

            ChatSection(
                userName = state.settings.userName,
                assistantName = state.settings.assistantName,
                userAvatar = state.settings.userAvatar,
                assistantAvatar = state.settings.assistantAvatar,
                exportMedia = state.settings.exportMedia,
                actions = actions,
            )

            // 4. APPEARANCE & PERSONALIZATION
            LanguageSection(state.settings.language, actions.setLanguage)

            Spacer(Modifier.height(8.dp))

            ThemeSection(
                accentColor = state.settings.accentColor,
                backgroundColor = state.settings.backgroundColor,
                fontScale = state.settings.fontScale,
                nameplate = state.settings.nameplate,
                actions = actions,
            )

            // 5. INFORMATION & STATUS
            val totalUsage = remember(state.mappings) {
                com.hatsyrei.maidnative.domain.TokenUsageSummary.fromNodes(state.mappings.values)
            }
            Text(tr("Token usage · saved history"), style = MaterialTheme.typography.titleMedium)
            Text(com.hatsyrei.maidnative.ui.chat.formatUsage(totalUsage),
                style = MaterialTheme.typography.bodyMedium)
            Text(tr("All saved chats and branches; deleted replies are excluded. Only server-reported usage is counted. Open a chat's Properties for details."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            TextButton(
                onClick = actions.clearTokenUsageHistory,
                enabled = !state.busy,
            ) {
                Text(tr("Clear token usage history"))
            }
        }
    }

    if (showPresets) {
        EndpointPresetSheet(
            presets = presets,
            activeBaseURL = state.settings.baseURL,
            activeApiKey = state.settings.apiKey,
            onSaveCurrent = {
                showPresets = false
                savingNew = true
            },
            onApply = {
                showPresets = false
                actions.applyPreset(it)
            },
            onRename = {
                showPresets = false
                naming = it
            },
            onDelete = {
                showPresets = false
                deleting = it
            },
            onDismiss = { showPresets = false },
        )
    }

    if (showScanOptions) {
        ScanOptionsDialog(
            port = state.settings.scanPort,
            prefixLength = state.settings.scanPrefixLength,
            onDismiss = { showScanOptions = false },
            onConfirm = { port, prefixLength ->
                showScanOptions = false
                actions.scan(port, prefixLength)
            },
        )
    }

    editingSampling?.let { group ->
        SamplingDialog(
            group = group,
            sampling = state.settings.sampling,
            onDismiss = { editingSampling = null },
            onConfirm = {
                editingSampling = null
                actions.setSampling(it)
            },
        )
    }

    if (showTools) {
        ToolsDialog(
            enabled = state.settings.enabledTools,
            ask = state.settings.askTools,
            onDismiss = { showTools = false },
            onConfirm = {
                showTools = false
                actions.setToolPolicies(it)
            },
        )
    }

    if (showMcp) {
        McpDialog(
            servers = mcpServers,
            listTools = actions.listMcpTools,
            onSave = actions.setMcpServers,
            onDismiss = { showMcp = false },
        )
    }

    if (savingNew) {
        PresetNameDialog(
            title = tr("Save endpoint"),
            confirmLabel = tr("Save"),
            initial = defaultPresetName(baseURL.text.toString()),
            takenNames = presets.map { it.name },
            onDismiss = { savingNew = false },
            onConfirm = {
                savingNew = false
                actions.savePreset(it, baseURL.text.toString(), apiKey.text.toString())
            },
        )
    }

    naming?.let { preset ->
        PresetNameDialog(
            title = tr("Rename preset"),
            confirmLabel = tr("Rename"),
            initial = preset.name,
            takenNames = presets.filter { it.id != preset.id }.map { it.name },
            onDismiss = { naming = null },
            onConfirm = {
                naming = null
                actions.renamePreset(preset.id, it)
            },
        )
    }

    deleting?.let { preset ->
        ConfirmDialog(
            title = tr("Delete preset"),
            message = tr("\"{0}\" and its stored API key will be removed from this device." , preset.name),
            onDismiss = { deleting = null },
            onConfirm = {
                deleting = null
                actions.deletePreset(preset.id)
            },
        )
    }
}

/**
 * Rewrites a field, parking the caret at whichever end should stay in view when
 * the value is wider than the field (the field scrolls to follow the caret).
 */
internal fun TextFieldState.replaceText(value: String, caretAtStart: Boolean = false) {
    edit {
        replace(0, length, value)
        selection = TextRange(if (caretAtStart) 0 else length)
    }
}

/**
 * An [OutlinedTextField] that commits its value on IME "Done" and when it loses
 * focus (only when the text actually differs from the last [committed] value).
 *
 * Deliberately the [TextFieldState] overload: the legacy `value`/`onValueChange`
 * one recomputes the dragged offset from the gesture's own deltas alone, so a
 * cursor or selection handle held at the edge of a value wider than the field
 * stops there instead of scrolling. The state-based field compensates for the
 * scroll it causes, which keeps the drag going (`handleDragPosition`).
 */
@Composable
internal fun AutoSaveTextField(
    state: TextFieldState,
    committed: String,
    label: String,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
    secure: Boolean = false,
    caretAtStart: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
) {
    // Adopt values the store writes behind the field's back (a scan result, a
    // loaded preset). No-op for the round trip of the field's own commit.
    LaunchedEffect(committed) {
        if (state.text.toString() != committed) state.replaceText(committed, caretAtStart)
    }

    var focused by remember { mutableStateOf(false) }
    // The field asks the scroller for its caret line only, and it asks before the
    // keyboard has taken its space, so a field near the bottom ends up half under
    // it. Re-ask for the whole box for as long as the inset is moving.
    val requester = remember { BringIntoViewRequester() }
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    LaunchedEffect(focused) {
        if (!focused) return@LaunchedEffect
        snapshotFlow { ime.getBottom(density) }.collectLatest { requester.bringIntoView() }
    }

    // [committed] lags a commit by a store round trip, so IME "Done" — which also
    // drops focus — would otherwise fire the same write a second time.
    var sent by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val commit = { value: String ->
        if (value != committed && value != sent) {
            sent = value
            onCommit(value)
        }
    }
    val fieldModifier = modifier
        .bringIntoViewRequester(requester)
        .onFocusChanged { focus ->
            if (focused && !focus.isFocused) commit(state.text.toString())
            focused = focus.isFocused
        }
    val onKeyboardAction = KeyboardActionHandler {
        commit(state.text.toString())
        focusManager.clearFocus()
    }
    if (secure) {
        OutlinedSecureTextField(
            state = state,
            label = { Text(label) },
            textObfuscationMode = TextObfuscationMode.Hidden,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = onKeyboardAction,
            modifier = fieldModifier,
        )
    } else {
        OutlinedTextField(
            state = state,
            label = { Text(label) },
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            modifier = fieldModifier,
        )
    }
}
