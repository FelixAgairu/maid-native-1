/*
 * Copyright (C) 2026 Felix Agairu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the License was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/.
 */

package com.hatsyrei.maidnative.domain

/** Labels are presentation only. The original, unsplit ID is always sent to the server. */
fun modelLabels(model: String): List<String> = model.split('-').filter { it.isNotEmpty() }
