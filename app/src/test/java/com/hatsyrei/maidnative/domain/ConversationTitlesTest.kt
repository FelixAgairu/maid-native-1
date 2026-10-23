package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.domain.tree.MessageTree
import org.junit.Assert.*
import org.junit.Test

class ConversationTitlesTest {
    private fun root(title: String = ConversationDefaults.CHAT_TITLE, manual: Boolean = false) =
        MessageTree.addNode(emptyMap(), "root", "system", "system", null, null, null,
            mapOf("title" to title) + if (manual) mapOf("titleSource" to "manual") else emptyMap())

    @Test fun `first prompt normalizes whitespace and supports Chinese`() {
        val result = ConversationTitles.apply(root(), "root", "  帮我\n写一个  安卓应用  ")
        assertEquals("帮我 写一个 安卓应用", result["root"]!!.metadata["title"])
        assertEquals("automatic", result["root"]!!.metadata["titleSource"])
    }

    @Test fun `attachment only prompt uses filename and empty prompt is ignored`() {
        assertEquals("报告.pdf", ConversationTitles.from("", "报告.pdf"))
        assertNull(ConversationTitles.from(" \n "))
    }

    @Test fun `manual titles and existing conversations are preserved`() {
        val manual = root(manual = true)
        assertSame(manual, ConversationTitles.apply(manual, "root", "hello"))
        val custom = root("My own title")
        assertSame(custom, ConversationTitles.apply(custom, "root", "hello"))
        val existing = MessageTree.addNode(root(), "user", "user", "old", "root", "root")
        assertSame(existing, ConversationTitles.apply(existing, "root", "new"))
    }

    @Test fun `title length is bounded without splitting surrogate pairs`() {
        val title = ConversationTitles.from("🐕".repeat(80))!!
        assertEquals(49, title.codePointCount(0, title.length))
        assertEquals("🐕".repeat(48) + "…", title)
    }
}
