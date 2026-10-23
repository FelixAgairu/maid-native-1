package com.hatsyrei.maidnative.ui.chat

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** A bounded viewport keeps enormous replies navigable without truncating their text. */
@Composable
internal fun ScrollableMessageBody(enabled: Boolean, identity: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by rememberSaveable(identity) { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    Column {
        Column(
            Modifier.fillMaxWidth().then(
                if (enabled && !expanded) Modifier.heightIn(max = 420.dp).verticalScroll(scroll) else Modifier,
            ),
            content = content,
        )
        if (enabled && (expanded || scroll.maxValue > 0)) {
            FlowRow {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) tr("Use scroll view") else tr("Expand all"))
                }
                if (!expanded) {
                    TextButton(onClick = { scope.launch { scroll.scrollTo(0) } }) { Text(tr("Top")) }
                    TextButton(onClick = { scope.launch { scroll.scrollTo(scroll.maxValue) } }) { Text(tr("Bottom")) }
                }
            }
        }
    }
}
