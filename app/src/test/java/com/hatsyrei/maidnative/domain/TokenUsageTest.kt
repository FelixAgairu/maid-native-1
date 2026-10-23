package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.data.store.MessageStore
import com.hatsyrei.maidnative.domain.tree.MessageNode
import org.junit.Assert.*
import org.junit.Test

class TokenUsageTest {
    @Test fun `repeated cumulative chunks replace counts but tool rounds add counts`() {
        val ledger = UsageLedger()
        ledger.begin()
        ledger.accept(TurnStats(promptTokens = 100, completionTokens = 4))
        ledger.accept(TurnStats(promptTokens = 100, completionTokens = 8))
        ledger.finish(true)
        ledger.begin()
        ledger.accept(TurnStats(promptTokens = 130, completionTokens = 12))
        ledger.finish(true)
        val result = TokenUsageSummary.of(ledger.snapshot())
        assertEquals(230L, result.input)
        assertEquals(20L, result.output)
        assertEquals(250L, result.total)
        assertEquals(2, result.completeRequests)
        assertFalse(result.partial)
    }

    @Test fun `aborted continuation does not inherit usage from previous round`() {
        val ledger = UsageLedger()
        ledger.begin()
        ledger.accept(TurnStats(promptTokens = 100, completionTokens = 8))
        ledger.finish(true)
        ledger.begin()
        val result = TokenUsageSummary.of(ledger.snapshot())
        assertEquals(108L, result.total)
        assertEquals(2, result.requests)
        assertEquals(1, result.completeRequests)
        assertTrue(result.partial)
    }

    @Test fun `missing usage remains unknown and genuine zero stays zero`() {
        val unknown = TokenUsageSummary.of(listOf(RequestUsage(finished = true)))
        assertNull(unknown.input)
        assertNull(unknown.output)
        assertNull(unknown.total)
        assertTrue(unknown.partial)
        val zero = TokenUsageSummary.of(listOf(RequestUsage(0, 0, true)))
        assertEquals(0L, zero.total)
        assertFalse(zero.partial)
    }

    @Test fun `independently arriving fields are merged and historical use survives edits and export`() {
        val ledger = UsageLedger()
        ledger.begin()
        ledger.accept(TurnStats(promptTokens = 100))
        ledger.accept(TurnStats(completionTokens = 8))
        ledger.finish(true)
        val metadata = TokenUsageStore.write(ledger.snapshot(), mapOf("title" to "test"))
        val node = MessageNode("a", "assistant", "edited body", "root", metadata = metadata)
        val exported = MessageStore.encodeExport(listOf(node)) { null }
        val imported = MessageStore.decodeExport(exported) { null }
        assertEquals(108L, TokenUsageSummary.fromNodes(imported).total)
        assertEquals("test", imported.single().metadata["title"])
    }

    @Test fun `totals exceed integer range safely and malformed metadata stays unknown`() {
        val result = TokenUsageSummary.of(List(3) { RequestUsage(Int.MAX_VALUE.toLong(), 1, true) })
        assertEquals(6442450944L, result.total)
        assertNull(TokenUsageStore.read(mapOf("requestUsage" to "broken")))
        val negative = TokenUsageStore.read(mapOf("requestUsage" to "[{\"input\":-9,\"output\":4}]"))!!
        assertNull(negative.single().input)
    }
}
