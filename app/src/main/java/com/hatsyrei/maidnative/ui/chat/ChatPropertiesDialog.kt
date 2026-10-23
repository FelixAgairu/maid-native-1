package com.hatsyrei.maidnative.ui.chat

import com.hatsyrei.maidnative.ui.i18n.tr
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hatsyrei.maidnative.domain.ChatStats
import com.hatsyrei.maidnative.domain.TokenUsageSummary
import com.hatsyrei.maidnative.domain.tools.LinkedFile
import com.hatsyrei.maidnative.ui.common.AppDialog

/**
 * Statistics and linked files for one conversation.
 *
 * Takes an already-computed [ChatStats] rather than the tree: every parameter
 * is then stable, so while a reply streams behind the dialog this skips instead
 * of recomposing once per token. The caller owns the `remember` that keeps the
 * aggregation itself from re-running.
 */
@Composable
internal fun ChatPropertiesDialog(
    title: String,
    stats: ChatStats,
    usage: TokenUsageSummary,
    allBranchesUsage: TokenUsageSummary,
    files: List<LinkedFile>,
    onOpenFile: (LinkedFile) -> Unit,
    onUnlinkFile: (LinkedFile) -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, maxLines = 2) },
        text = {
            Column {
                Text(tr("Token usage · active branch"), style = MaterialTheme.typography.titleSmall)
                StatRow(tr("Input tokens"), usage.input?.formatted() ?: UNKNOWN)
                StatRow(tr("Output tokens"), usage.output?.formatted() ?: UNKNOWN)
                StatRow(if (usage.partial) tr("Known total (partial)") else tr("Total tokens"), usage.total?.formatted() ?: UNKNOWN)
                StatRow(tr("Complete usage records"), "${usage.completeRequests} / ${usage.requests}")
                Text(tr("All retained branches"), style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 16.dp))
                Text(formatUsage(allBranchesUsage), style = MaterialTheme.typography.bodySmall)
                Note(tr("Usage sums API requests, including tool continuations and repeated input history. Missing or interrupted records remain partial. Deleted replies are excluded; this is not a billing ledger."))
                if (allBranchesUsage.legacyTurns > 0) Note(tr("Older tool replies may only include the final request's usage."))
                StatRow(
                    tr("Conversation size"),
                    stats.conversationTokens.tokens(stats.sizeAccuracy),
                )
                StatRow(tr("Average speed"), stats.tokensPerSecond.tokensPerSecond())
                StatRow(tr("Average response"), stats.averageResponseMs.duration())
                StatRow(tr("Time to first token"), stats.averageTtftMs.duration())
                val notes = stats.notes()
                if (notes.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        notes.forEach { Note(it) }
                    }
                }
                if (files.isNotEmpty()) {
                    Text(
                        text = tr("Linked files"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                    )
                    LinkedFileRows(files = files, onOpen = onOpenFile, onUnlink = onUnlinkFile)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(tr("Close")) }
        },
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Note(text: String) {
    Row {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Everything the figures above need qualified, in the order they appear. */
private fun ChatStats.notes(): List<String> = buildList {
    sizeNote()?.let { add(it) }
    if (partialSample) {
        add(
            tr("Averages cover {0} of {1} replies; metadata unavailable for the rest." , statsTurns, assistantMessages),
        )
    }
}

private fun ChatStats.sizeNote(): String? {
    if (conversationTokens == null) return null
    return when (sizeAccuracy) {
        ChatStats.SizeAccuracy.EXACT -> null
        ChatStats.SizeAccuracy.AT_LEAST ->
            tr("The conversation has grown since it was last counted; the next reply will update it.")
        ChatStats.SizeAccuracy.AT_MOST ->
            tr("The conversation has shrunk since it was last counted; the next reply will update it.")
    }
}

private fun Int?.tokens(accuracy: ChatStats.SizeAccuracy): String = when {
    this == null -> UNKNOWN
    accuracy == ChatStats.SizeAccuracy.AT_LEAST -> "\u2265 ${formatted()} tokens"
    accuracy == ChatStats.SizeAccuracy.AT_MOST -> "\u2264 ${formatted()} tokens"
    else -> "${formatted()} tokens"
}

private fun Double?.tokensPerSecond(): String =
    if (this == null) UNKNOWN else formatTokensPerSecond(this)

