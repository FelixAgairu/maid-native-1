package com.hatsyrei.maidnative.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.ui.common.AppDialog
import com.hatsyrei.maidnative.ui.i18n.tr
import org.json.JSONArray
import java.util.Locale

@Composable
internal fun LanguageSection(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val languages = remember { context.assets.open("i18n/languages.json").bufferedReader().use {
        val array = JSONArray(it.readText()); (0 until array.length()).map(array::getString)
    } }
    fun nativeName(tag: String): String = Locale.forLanguageTag(tag).let { it.getDisplayName(it) }
    var open by remember { mutableStateOf(false) }
    TextButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Text(tr("Language") + " · " + if (selected.isEmpty()) tr("System default") else nativeName(selected))
    }
    if (open) {
        var query by remember { mutableStateOf("") }
        AppDialog(onDismissRequest = { open = false }, title = { Text(tr("Language")) },
            confirmButton = { TextButton(onClick = { open = false }) { Text(tr("Close")) } },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(query, { query = it }, label = { Text(tr("Search languages")) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    TextButton(onClick = { onSelect(""); open = false }) { Text(tr("System default")) }
                    languages.filter {
                        val locale = Locale.forLanguageTag(it)
                        (nativeName(it) + " " + locale.getDisplayName(Locale.ENGLISH) + " " + locale.displayName + " " + it)
                            .contains(query.trim(), ignoreCase = true)
                    }.forEach { tag ->
                        TextButton(onClick = { onSelect(tag); open = false }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (tag == selected) "✓ " else "") + nativeName(tag), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            })
    }
}
