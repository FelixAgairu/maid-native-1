package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.domain.tree.MessageNode
import com.hatsyrei.maidnative.domain.tools.toolCalls
import org.json.JSONArray
import org.json.JSONObject

/** Server-reported cumulative counters for one HTTP request, not one SSE chunk. */
data class RequestUsage(val input: Long? = null, val output: Long? = null, val finished: Boolean = false)

/** Repeated usage chunks replace counters; tool continuation requests add a new record. */
class UsageLedger {
    private val requests = mutableListOf<RequestUsage>()
    fun reset() = requests.clear()
    fun begin() { requests += RequestUsage() }
    fun accept(stats: TurnStats) {
        if (requests.isEmpty()) return
        val old = requests.last()
        requests[requests.lastIndex] = old.copy(
            input = stats.promptTokens?.toLong() ?: old.input,
            output = stats.completionTokens?.toLong() ?: old.output,
        )
    }
    fun finish(success: Boolean) {
        if (requests.isNotEmpty()) requests[requests.lastIndex] = requests.last().copy(finished = success)
    }
    fun snapshot(): List<RequestUsage> = requests.toList()
}

object TokenUsageStore {
    private const val KEY = "requestUsage"
    fun write(requests: List<RequestUsage>, metadata: Map<String, Any?>): Map<String, Any?> =
        metadata + (KEY to JSONArray(requests.map {
            JSONObject().put("input", it.input).put("output", it.output).put("finished", it.finished)
        }).toString())

    fun read(metadata: Map<String, Any?>): List<RequestUsage>? {
        val raw = metadata[KEY] as? String ?: return null
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val entry = array.getJSONObject(i)
                fun count(key: String) = (entry.opt(key) as? Number)?.toLong()?.takeIf { it >= 0 }
                RequestUsage(count("input"), count("output"), entry.optBoolean("finished"))
            }
        }.getOrNull()
    }
}

/** Historical usage of retained replies. It deliberately does not estimate missing tokens. */
data class TokenUsageSummary(
    val input: Long?,
    val output: Long?,
    val requests: Int,
    val completeRequests: Int,
    val legacyTurns: Int = 0,
) {
    val total: Long? get() = if (input == null && output == null) null else (input ?: 0L) + (output ?: 0L)
    val partial: Boolean get() = completeRequests < requests || legacyTurns > 0

    operator fun plus(other: TokenUsageSummary): TokenUsageSummary {
        fun sum(a: Long?, b: Long?): Long? =
            if (a == null && b == null) null else (a ?: 0L) + (b ?: 0L)
        return TokenUsageSummary(sum(input, other.input), sum(output, other.output),
            requests + other.requests, completeRequests + other.completeRequests,
            legacyTurns + other.legacyTurns)
    }

    companion object {
        fun of(requests: List<RequestUsage>, legacyTurns: Int = 0): TokenUsageSummary {
            val inputs = requests.mapNotNull { it.input }
            val outputs = requests.mapNotNull { it.output }
            return TokenUsageSummary(
                inputs.takeIf { it.isNotEmpty() }?.sum(),
                outputs.takeIf { it.isNotEmpty() }?.sum(),
                requests.size,
                requests.count { it.finished && it.input != null && it.output != null },
                legacyTurns,
            )
        }

        fun fromNodes(nodes: Collection<MessageNode>): TokenUsageSummary {
            val requests = mutableListOf<RequestUsage>()
            var legacy = 0
            for (node in nodes) {
                if (node.role != "assistant") continue
                val records = TokenUsageStore.read(node.metadata)
                if (records != null && records.isNotEmpty()) {
                    requests += records
                } else {
                    val stats = node.stats()
                    // Old tool replies only kept the final request; label their totals as partial.
                    val oldToolTurn = node.toolCalls().isNotEmpty()
                    if (oldToolTurn) legacy++
                    requests += RequestUsage(stats.promptTokens?.toLong(), stats.completionTokens?.toLong(),
                        !stats.stopped && !oldToolTurn)
                }
            }
            return of(requests, legacy)
        }
    }
}

/** Cache the retained history once per tree change; only live usage changes while streaming. */
data class ConversationTokenUsage(val saved: TokenUsageSummary, val streamingId: String?) {
    fun withLiveUsage(id: String?, usage: TokenUsageSummary?): TokenUsageSummary {
        if (streamingId == null) return saved
        val pending = TokenUsageSummary.of(listOf(RequestUsage()))
        return saved + (if (id == streamingId) usage ?: pending else pending)
    }

    companion object {
        fun fromNodes(nodes: Collection<MessageNode>, root: String?, streamingId: String?): ConversationTokenUsage {
            val conversation = nodes.filter { it.root == root }
            val activeId = streamingId?.takeIf { id ->
                conversation.any { it.id == id && it.role == "assistant" }
            }
            return ConversationTokenUsage(
                TokenUsageSummary.fromNodes(conversation.filter { it.id != activeId }), activeId,
            )
        }
    }
}
