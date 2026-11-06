/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.data

import org.json.JSONObject

/**
 * Shared JSON conversion helpers for the metadata/node (de)serialization used
 * by both Room ([com.hatsyrei.maidnative.data.db.MessageEntity]) and the
 * export/import store ([com.hatsyrei.maidnative.data.store.MessageStore]).
 */

/** Read [key] as a non-blank string, or `null` when absent, JSON-null, or empty. */
internal fun JSONObject.optStringOrNull(key: String): String? =
    if (isNull(key) || !has(key)) null else optString(key).ifEmpty { null }

/** Convert to a `Map`, mapping JSON-null values to Kotlin `null`. Order preserved. */
internal fun JSONObject.toMap(): Map<String, Any?> {
    val map = LinkedHashMap<String, Any?>()
    val keys = keys()
    while (keys.hasNext()) {
        val k = keys.next()
        val v = get(k)
        map[k] = if (v === JSONObject.NULL) null else v
    }
    return map
}

/** Build a [JSONObject], mapping Kotlin `null` values back to JSON-null. */
internal fun Map<String, Any?>.toJsonObject(): JSONObject =
    JSONObject(mapValues { it.value ?: JSONObject.NULL })
