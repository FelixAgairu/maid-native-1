/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.domain

import com.hatsyrei.maidnative.domain.tree.Mappings
import com.hatsyrei.maidnative.domain.tree.MessageTree

/** Local first-message titles: no extra network request and no additional token charge. */
object ConversationTitles {
    fun from(text: String, attachmentName: String? = null): String? {
        val title = (text.ifBlank { attachmentName.orEmpty() })
            .replace(Regex("[\\s\\p{Z}]+"), " ")
            .replace(Regex("[\\p{Cc}]"), "")
            .trim()
        if (title.isEmpty()) return null
        val count = title.codePointCount(0, title.length)
        return if (count <= 48) title else title.substring(0, title.offsetByCodePoints(0, 48)).trimEnd() + "…"
    }

    fun apply(mappings: Mappings, rootId: String, text: String, attachmentName: String? = null): Mappings {
        val root = mappings[rootId] ?: return mappings
        if (root.metadata["titleSource"] != null) return mappings
        val previous = root.metadata["title"] as? String
        if (!previous.isNullOrBlank() && previous != ConversationDefaults.CHAT_TITLE) return mappings
        // Only the first user message may set a title, including an attachment-only first message.
        if (mappings.values.any { it.root == rootId && it.role == "user" }) return mappings
        val title = from(text, attachmentName) ?: return mappings
        return MessageTree.updateContent(mappings, rootId, { it }, metadata = {
            it + mapOf("title" to title, "titleSource" to "automatic")
        })
    }
}
