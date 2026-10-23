package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.domain.tree.MessageNode
import java.security.MessageDigest

/** Current branch size, never the sum of historical prompt counts. */
data class ContextUsage(val tokens: Long, val estimated: Boolean, val mediaUncounted: Boolean = false) {
    companion object {
        const val SIGNATURE = "contextSignature"
        fun signature(nodes: List<MessageNode>, endpoint: String, model: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            fun field(value: String) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(bytes.size.toString().toByteArray()); digest.update(0.toByte()); digest.update(bytes)
            }
            field(endpoint); field(model)
            nodes.forEach { field(it.id); field(it.role); field(it.content); field(it.attachments.toString()) }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        /** A deliberately labelled fallback, not a model-specific tokenizer. */
        fun estimate(text: String): Long {
            var latin = 0L
            var other = 0L
            text.codePoints().forEach { if (it < 0x2E80) latin++ else other++ }
            return (latin + 3) / 4 + other
        }

        fun of(
            nodes: List<MessageNode>, endpoint: String, model: String, draft: String = "",
            liveId: String? = null, liveText: String = "", liveUsage: RequestUsage? = null,
        ): ContextUsage {
            val last = nodes.lastOrNull()
            val live = liveId != null && last?.id == liveId
            val media = nodes.any { it.attachments.isNotEmpty() }
            val measured = if (live) liveUsage else last?.takeIf { it.role == "assistant" }?.let {
                val records = TokenUsageStore.read(it.metadata)
                records?.lastOrNull() ?: it.stats().let { stats ->
                    RequestUsage(stats.promptTokens?.toLong(), stats.completionTokens?.toLong(), !stats.stopped)
                }
            }
            val matches = live || (last?.metadata?.get(SIGNATURE) == signature(nodes, endpoint, model))
            if (matches && measured?.input != null && measured.output != null) {
                return ContextUsage(measured.input + measured.output + estimate(draft),
                    draft.isNotEmpty() || !measured.finished, false)
            }
            val textTokens = nodes.sumOf { node ->
                4L + estimate(if (live && node.id == liveId) liveText else node.content)
            } + estimate(draft)
            return ContextUsage(textTokens, true, media)
        }
    }
}
