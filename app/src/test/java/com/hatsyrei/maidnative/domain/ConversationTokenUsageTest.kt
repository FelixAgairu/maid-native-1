package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.domain.tree.MessageNode
import org.junit.Assert.*
import org.junit.Test

class ConversationTokenUsageTest {
    private fun reply(id: String, root: String = "chat", input: Long = 120, output: Long = 240) =
        MessageNode(id, "assistant", "reply", root,
            metadata = TokenUsageStore.write(listOf(RequestUsage(input, output, true)), emptyMap()))

    @Test fun `current conversation includes retained branches but excludes other chats and user messages`() {
        val nodes = listOf(reply("a"), reply("alternative"), reply("other", "another-chat"),
            reply("user").copy(role = "user"))
        val usage = ConversationTokenUsage.fromNodes(nodes, "chat", null).withLiveUsage(null, null)
        assertEquals(720L, usage.total)
        assertEquals(240L, usage.input)
        assertEquals(480L, usage.output)
        assertEquals(2, usage.requests)
        assertFalse(usage.partial)
    }

    @Test fun `stream replaces placeholder and final persistence never counts it twice`() {
        val nodes = listOf(reply("previous"), reply("live").copy(metadata = emptyMap()))
        val history = ConversationTokenUsage.fromNodes(nodes, "chat", "live")
        val pending = history.withLiveUsage("live", null)
        assertEquals(360L, pending.total)
        assertEquals(2, pending.requests)
        assertTrue(pending.partial)
        val progress = TokenUsageSummary.of(listOf(RequestUsage(120, 10)))
        assertEquals(490L, history.withLiveUsage("live", progress).total)
        val finished = TokenUsageSummary.of(listOf(RequestUsage(120, 240, true)))
        val during = history.withLiveUsage("live", finished)
        val saved = ConversationTokenUsage.fromNodes(listOf(reply("previous"), reply("live")), "chat", null)
        assertEquals(during, saved.withLiveUsage("live", finished))
        assertEquals(720L, during.total)
        assertFalse(during.partial)
    }

    @Test fun `switching conversations ignores the stream in the previous chat`() {
        val nodes = listOf(reply("live"), reply("other", "another-chat", 8, 2))
        val other = ConversationTokenUsage.fromNodes(nodes, "another-chat", "live")
        assertNull(other.streamingId)
        assertEquals(10L, other.withLiveUsage("live", TokenUsageSummary.of(listOf(RequestUsage(999, 1)))).total)
        val empty = ConversationTokenUsage.fromNodes(nodes, "empty-chat", "live")
        assertEquals(0, empty.withLiveUsage("live", null).requests)
        assertFalse(empty.saved.partial)
    }

    @Test fun `unknown stopped and zero usage remain distinct and deletion removes counts`() {
        val unknown = reply("unknown").copy(metadata = emptyMap())
        val zero = reply("zero", input = 0, output = 0)
        val stopped = reply("stopped").copy(metadata =
            TokenUsageStore.write(listOf(RequestUsage(100, null, false)), emptyMap()))
        val onlyUnknown = ConversationTokenUsage.fromNodes(listOf(unknown), "chat", null).saved
        assertNull(onlyUnknown.total)
        assertTrue(onlyUnknown.partial)
        val total = ConversationTokenUsage.fromNodes(listOf(unknown, zero, stopped), "chat", null).saved
        assertEquals(100L, total.total)
        assertEquals(1, total.completeRequests)
        assertTrue(total.partial)
        val afterDelete = ConversationTokenUsage.fromNodes(listOf(zero), "chat", null).saved
        assertEquals(0L, afterDelete.total)
        assertFalse(afterDelete.partial)
    }

    @Test fun `stale live id cannot replace usage of the current pending request`() {
        val history = ConversationTokenUsage.fromNodes(listOf(reply("current")), "chat", "current")
        val usage = history.withLiveUsage("previous", TokenUsageSummary.of(listOf(RequestUsage(100, 900, true))))
        assertNull(usage.total)
        assertEquals(1, usage.requests)
        assertTrue(usage.partial)
    }
}
