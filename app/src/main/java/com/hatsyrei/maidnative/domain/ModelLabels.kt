package com.hatsyrei.maidnative.domain

/** Labels are presentation only. The original, unsplit ID is always sent to the server. */
fun modelLabels(model: String): List<String> = model.split('-').filter { it.isNotEmpty() }
