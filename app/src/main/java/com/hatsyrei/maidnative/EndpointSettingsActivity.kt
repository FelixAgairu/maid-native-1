/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.hatsyrei.maidnative.data.prefs.SettingsRepository
import com.hatsyrei.maidnative.ui.chat.ChatUiState
import com.hatsyrei.maidnative.ui.i18n.AppLanguage
import com.hatsyrei.maidnative.ui.i18n.tr
import com.hatsyrei.maidnative.ui.settings.EndpointSettingsScreen
import com.hatsyrei.maidnative.ui.settings.SettingsActions
import com.hatsyrei.maidnative.ui.theme.MaidNativeTheme
import com.hatsyrei.maidnative.ui.theme.ThemeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EndpointSettingsActivity : ComponentActivity() {
    private val settingsRepo = SettingsRepository(this)
    private val scope = CoroutineScope(Dispatchers.Main)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsRepo.settings.collectAsState(SettingsRepository.Settings())
            val presets by settingsRepo.presets.collectAsState(emptyList())

            val state = ChatUiState(
                settings = settings,
                models = emptyList(),
                busy = false,
                scanning = false,
                foundURL = null,
                credentialNotice = null,
            )

            val actions = remember {
                SettingsActions(
                    setLanguage = { },
                    setBaseURL = { scope.launch { settingsRepo.setBaseURL(it) } },
                    setApiKey = { scope.launch { settingsRepo.setApiKey(it) } },
                    setAllowCleartext = { },
                    setUseCustomCertificates = { },
                    setTrustUserCertificates = { },
                    setAutoRenameChats = { },
                    setModelPickerTags = { },
                    setScrollLongMessages = { },
                    setAlwaysShowScrollbar = { },
                    setCollapseMessages = { },
                    setModel = { },
                    refreshModels = { },
                    setReasoning = { },
                    setSampling = { },
                    setToolPolicies = { },
                    setMcpServers = { },
                    listMcpTools = { _, _ -> emptyList() },
                    scan = { _, _ -> },
                    resetScan = {},
                    savePreset = { name, baseURL, apiKey ->
                        scope.launch {
                            settingsRepo.savePreset(name, baseURL, apiKey)
                        }
                    },
                    applyPreset = { scope.launch { settingsRepo.applyPreset(it) } },
                    renamePreset = { id, name ->
                        scope.launch {
                            settingsRepo.renamePreset(id, name)
                        }
                    },
                    deletePreset = { id ->
                        scope.launch {
                            settingsRepo.deletePreset(id)
                        }
                    },
                    setAccentColor = { },
                    setBackgroundColor = { },
                    setNameplate = { },
                    importNameplate = { },
                    setUserName = { },
                    setAssistantName = { },
                    importAvatar = { _, _ -> },
                    removeAvatar = { },
                    setExportMedia = { },
                    setFontScale = { },
                    clearTokenUsageHistory = { },
                    setCustomCertificatePath = { },
                )
            }

            AppLanguage(settings.language) {
                MaidNativeTheme(
                    theme = ThemeSettings(
                        accentColor = settings.accentColor,
                        backgroundColor = settings.backgroundColor,
                        fontScale = settings.fontScale,
                    ),
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            TopAppBar(
                                title = { Text(tr("Endpoint")) },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back"))
                                    }
                                },
                            )
                        },
                    ) { innerPadding ->
                        EndpointSettingsScreen(
                            state = state,
                            presets = presets,
                            actions = actions,
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}