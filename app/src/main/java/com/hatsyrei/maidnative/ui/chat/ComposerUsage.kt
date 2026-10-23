package com.hatsyrei.maidnative.ui.chat

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.domain.ConversationTokenUsage
import com.hatsyrei.maidnative.domain.ContextUsage
import com.hatsyrei.maidnative.domain.tree.MessageTree
import com.hatsyrei.maidnative.domain.tree.MessageNode
import com.hatsyrei.maidnative.domain.tree.Mappings
import com.hatsyrei.maidnative.ui.common.AppDialog

/** Reads the stream in an isolated scope: text deltas do not redraw the composer or message list. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ComposerUsage(
    mappings: Mappings,
    root: String?,
    streamingId: String?,
    streaming: State<StreamingText?>,
    endpoint: String,
    model: String,
    contextWindow: Long?,
    draft: String,
    pendingAttachments: Boolean = false,
    systemPrompt: String = "",
    compact: Boolean = false,
) {
    val history = remember(mappings, root, streamingId) {
        ConversationTokenUsage.fromNodes(mappings.values, root, streamingId)
    }
    val usage by remember(history, streaming) {
        derivedStateOf {
            val live = streaming.value
            history.withLiveUsage(live?.id, live?.usage)
        }
    }
    var showDetails by remember(root) { mutableStateOf(false) }
    val branch = remember(mappings, root, systemPrompt) {
        root?.let { MessageTree.getConversation(mappings, it) }
            ?: listOf(MessageNode("__preview__", "system", systemPrompt, "__preview__"))
    }
    val context by remember(branch, endpoint, model, draft, streamingId, streaming, pendingAttachments) {
        derivedStateOf {
            val live = streaming.value?.takeIf { it.id == streamingId && it.endpoint == endpoint && it.model == model }
            ContextUsage.of(branch, endpoint, model, draft, live?.id,
                live?.text.orEmpty() + live?.reasoning.orEmpty(), live?.contextUsage).let {
                    if (pendingAttachments) it.copy(estimated = true, mediaUncounted = true) else it
                }
        }
    }
    val contextCount = (if (context.estimated) "≈" else "") + context.tokens.formatted()
    val contextLabel = if (contextWindow != null) {
        tr("Context · {0} / {1} tokens ({2}%)" , contextCount, contextWindow.formatted(), (100.0 * context.tokens / contextWindow).toInt())
    } else tr("Context · {0} tokens" , contextCount)
    val empty = usage.requests == 0
    val total = if (empty) "0" else usage.total?.formatted() ?: "N/A"
    val input = if (empty) "0" else usage.input?.formatted() ?: "N/A"
    val output = if (empty) "0" else usage.output?.formatted() ?: "N/A"
    val status = when {
        history.streamingId != null -> tr(" · live")
        usage.partial -> tr(" · partial")
        else -> ""
    }
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides if (compact) 40.dp else 48.dp) {
        TextButton(
            onClick = { showDetails = true },
            modifier = Modifier.fillMaxWidth().semantics {
                contentDescription = contextLabel + ". " + tr("Conversation token usage: {0} tokens{1}. Input {2}, output {3}. View details" , total, status, input, output)
            },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        ) {
            if (compact) {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(contextLabel, style = MaterialTheme.typography.labelSmall)
                    Text(tr("Chat · {0} tokens{1}", total, status), style = MaterialTheme.typography.labelSmall)
                    Text(tr("In {0} · Out {1}", input, output), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(contextLabel, style = MaterialTheme.typography.labelSmall)
                    if (contextWindow != null) LinearProgressIndicator(
                        progress = { (context.tokens.toFloat() / contextWindow).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tr("Chat · {0} tokens{1}" , total, status), style = MaterialTheme.typography.labelSmall)
                        Text(tr("In {0} · Out {1}" , input, output), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (showDetails) {
        AppDialog(
            onDismissRequest = { showDetails = false },
            title = { Text(tr("Conversation token usage")) },
            confirmButton = { TextButton(onClick = { showDetails = false }) { Text(tr("Close")) } },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(contextLabel, style = MaterialTheme.typography.titleMedium)
                    Text(if (context.estimated) tr("≈ is an estimate of the current branch and draft. Tokenization, tool schemas and media can change the actual count.")
                        else tr("Context uses the latest request's input + output, not the sum of all turns."))
                    if (context.mediaUncounted) Text(tr("Media token costs are not included in the estimate."))
                    if (contextWindow == null) Text(tr("The server did not report a context capacity."))
                    Text(tr("Total · {0} tokens" , total), style = MaterialTheme.typography.titleLarge)
                    Text(tr("Input · {0}\nOutput · {1}" , input, output))
                    Text(tr("Requests with complete usage · {0} / {1}" , usage.completeRequests, usage.requests))
                    if (history.streamingId != null) {
                        Text(tr("Updating as the server reports usage. Some servers only report it when a reply finishes."))
                    }
                    if (usage.partial) {
                        Text(tr("Partial: totals include only known counts. Some requests are unfinished or have missing usage."))
                    }
                    Text(tr("Cumulative input + output across all retained branches of this conversation, including tool requests. Deleted replies are excluded."))
                    Text(tr("Input includes history sent again on each request. This is usage, not the number of tokens currently in the model's context window."))
                }
            },
        )
    }
}
