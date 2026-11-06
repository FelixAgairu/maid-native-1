/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.ui.settings

import android.net.Uri
import androidx.compose.runtime.Stable
import com.hatsyrei.maidnative.data.prefs.SettingsRepository.EndpointPreset
import com.hatsyrei.maidnative.data.store.AvatarStore
import com.hatsyrei.maidnative.domain.Sampling
import com.hatsyrei.maidnative.domain.tools.McpServer
import com.hatsyrei.maidnative.domain.tools.McpToolInfo
import com.hatsyrei.maidnative.domain.tools.ToolPolicy

/**
 * Everything the settings screen can ask the view model to do. Grouped for the
 * same reason as `ChatActions`: loose lambdas made every section unskippable
 * and spread one feature across three parameter lists.
 */
@Stable
class SettingsActions(
    val setLanguage: (String) -> Unit,
    val setBaseURL: (String) -> Unit,
    val setApiKey: (String) -> Unit,
    val setAllowCleartext: (Boolean) -> Unit,
    val setUseCustomCertificates: (Boolean) -> Unit,
    val setTrustUserCertificates: (Boolean) -> Unit,
    val setAutoRenameChats: (Boolean) -> Unit,
    val setModelPickerTags: (Boolean) -> Unit,
    val setScrollLongMessages: (Boolean) -> Unit,
    val setAlwaysShowScrollbar: (Boolean) -> Unit,
    val setCollapseMessages: (Boolean) -> Unit,
    val setModel: (String) -> Unit,
    val refreshModels: () -> Unit,
    val setReasoning: (Boolean) -> Unit,
    val setSampling: (Sampling) -> Unit,
    val setToolPolicies: (Map<String, ToolPolicy>) -> Unit,
    val setMcpServers: (List<McpServer>) -> Unit,
    val listMcpTools: suspend (url: String, token: String) -> List<McpToolInfo>,
    val scan: (port: Int, prefixLength: Int) -> Unit,
    val resetScan: () -> Unit,
    val savePreset: (name: String, baseURL: String, apiKey: String) -> Unit,
    val applyPreset: (EndpointPreset) -> Unit,
    val renamePreset: (id: String, name: String) -> Unit,
    val deletePreset: (String) -> Unit,
    val setAccentColor: (Int) -> Unit,
    val setBackgroundColor: (Int) -> Unit,
    val setNameplate: (String) -> Unit,
    val importNameplate: (Uri) -> Unit,
    val setUserName: (String) -> Unit,
    val setAssistantName: (String) -> Unit,
    val importAvatar: (AvatarStore.Role, Uri) -> Unit,
    val removeAvatar: (AvatarStore.Role) -> Unit,
    val setExportMedia: (Boolean) -> Unit,
    val setFontScale: (Float) -> Unit,
    val clearTokenUsageHistory: () -> Unit,
    val setCustomCertificatePath: (String) -> Unit,
)
