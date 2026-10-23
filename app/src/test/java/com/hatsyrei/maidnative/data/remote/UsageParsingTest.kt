package com.hatsyrei.maidnative.data.remote

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class UsageParsingTest {
    private val client = OpenAiClient()
    @Test fun `parses both token field conventions without inventing unknown values`() {
        val stats = client.turnStats(JSONObject("{\"input_tokens\":123,\"output_tokens\":45}"), null)
        assertEquals(123, stats.promptTokens)
        assertEquals(45, stats.completionTokens)
        val empty = client.turnStats(JSONObject("{\"prompt_tokens\":null,\"completion_tokens\":-4}"), null)
        assertNull(empty.promptTokens)
        assertNull(empty.completionTokens)
    }
    @Test fun `invalid counts do not become zero or overflow`() {
        val stats = client.turnStats(JSONObject("{\"prompt_tokens\":\"bad\",\"completion_tokens\":4294967297}"), null)
        assertNull(stats.promptTokens)
        assertNull(stats.completionTokens)
    }
}
