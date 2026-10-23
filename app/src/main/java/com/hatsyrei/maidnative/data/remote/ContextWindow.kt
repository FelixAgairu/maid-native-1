package com.hatsyrei.maidnative.data.remote

import org.json.JSONObject

/** Only explicit capacity fields; parameter counts and model-name suffixes are not capacities. */
internal fun JSONObject.contextWindow(): Long? {
    fun JSONObject.positive(key: String) = (opt(key) as? Number)?.toLong()?.takeIf { it in 1..Int.MAX_VALUE.toLong() }
    return positive("context_length") ?: positive("max_model_len") ?: positive("n_ctx")
        ?: optJSONObject("architecture")?.positive("context_length")
        ?: optJSONObject("default_generation_settings")?.positive("n_ctx")
}
