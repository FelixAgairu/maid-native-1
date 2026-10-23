package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.domain.tree.MessageNode
import org.junit.Assert.*
import org.junit.Test

class ContextUsageTest {
    private fun conversation(): List<MessageNode> {
        val system = MessageNode("root", "system", "Be helpful", "root")
        val user = MessageNode("user", "user", "Hello", "root", parent = "root")
        val answer = MessageNode("a", "assistant", "Answer", "root", parent = "user",
            metadata = TokenUsageStore.write(listOf(RequestUsage(120, 240, true)), emptyMap()))
        val nodes = listOf(system, user, answer)
        return nodes.dropLast(1) + answer.copy(metadata = answer.metadata + (ContextUsage.SIGNATURE to ContextUsage.signature(nodes, "url", "model")))
    }

    @Test fun `context uses latest request not cumulative usage of tool rounds`() {
        val nodes = conversation().toMutableList()
        nodes[2] = nodes[2].copy(metadata = TokenUsageStore.write(listOf(RequestUsage(120, 240, true), RequestUsage(200, 20, true)),nodes[2].metadata))
        val result = ContextUsage.of(nodes, "url", "model")
        assertEquals(220L,result.tokens)
        assertFalse(result.estimated)
        assertEquals(580L,TokenUsageSummary.fromNodes(nodes).total)
    }

    @Test fun `same length edits and model switches invalidate measured context`() {
        val nodes = conversation()
        assertFalse(ContextUsage.of(nodes,"url","model").estimated)
        val edited = nodes.toMutableList().apply { this[1] = this[1].copy(content = "World") }
        assertTrue(ContextUsage.of(edited,"url","model").estimated)
        assertTrue(ContextUsage.of(nodes,"url","different-model").estimated)
        assertTrue(ContextUsage.of(nodes,"different-url","model").estimated)
    }

    @Test fun `draft is included and explicitly estimated without changing history`() {
        val nodes=conversation()
        val estimate=ContextUsage.of(nodes,"url","model","abcd")
        assertEquals(361L,estimate.tokens)
        assertTrue(estimate.estimated)
        assertEquals(360L,TokenUsageSummary.fromNodes(nodes).total)
    }

    @Test fun `live usage replaces snapshot only for the active branch leaf`() {
        val nodes=conversation()
        val live=ContextUsage.of(nodes,"url","model",liveId="a",liveUsage=RequestUsage(130,10))
        assertEquals(140L,live.tokens)
        assertTrue(live.estimated)
        val other=ContextUsage.of(nodes,"url","model",liveId="other",liveUsage=RequestUsage(9999,9999))
        assertEquals(360L,other.tokens)
        assertFalse(other.estimated)
    }

    @Test fun `missing usage still displays a labelled estimate and unicode is counted`() {
        val nodes=conversation().map { it.copy(metadata=emptyMap()) }
        assertTrue(ContextUsage.of(nodes,"url","model").tokens>0)
        assertTrue(ContextUsage.of(nodes,"url","model").estimated)
        assertEquals(4L,ContextUsage.estimate("你好世界"))
    }

    @Test fun `hyphen labels preserve the actual model identifier`() {
        assertEquals(listOf("Qwen3.8","27B","Reasoning"),modelLabels("Qwen3.8-27B-Reasoning"))
        assertEquals(listOf("vendor/Qwen3.8","27B","Q4_K_M"),modelLabels("vendor/Qwen3.8-27B-Q4_K_M"))
        assertEquals(listOf("Qwen","27B"),modelLabels("Qwen--27B-"))
    }
}
