package com.hatsyrei.maidnative.data.remote

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ContextWindowTest {
    @Test fun `reads explicit model and llama server capacity`() {
        assertEquals(32768L,JSONObject("{\"context_length\":32768}").contextWindow())
        assertEquals(8192L,JSONObject("{\"max_model_len\":8192}").contextWindow())
        assertEquals(4096L,JSONObject("{\"default_generation_settings\":{\"n_ctx\":4096}}").contextWindow())
    }
    @Test fun `never infers configured capacity from parameter count or training length`() {
        assertNull(JSONObject("{\"id\":\"Qwen-27B-256K\",\"meta\":{\"n_ctx_train\":262144}}").contextWindow())
        assertNull(JSONObject("{\"context_length\":-1}").contextWindow())
        assertNull(JSONObject("{\"context_length\":0}").contextWindow())
    }
}
