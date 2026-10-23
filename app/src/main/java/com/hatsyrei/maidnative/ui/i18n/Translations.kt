package com.hatsyrei.maidnative.ui.i18n

import android.content.res.Configuration
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.LocaleList
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.json.JSONObject
import org.json.JSONArray
import java.util.Locale

/** Only UI-owned strings. Never translate chat content, model identifiers or protocol fields. */
internal object Translations {
    var strings by mutableStateOf<Map<String, String>>(emptyMap())
    private val placeholder = Regex("\\{(\\d+)\\}")
    fun format(template: String, args: Array<out Any?>): String = placeholder.replace(template) {
        args.getOrNull(it.groupValues[1].toInt())?.toString() ?: it.value
    }
}
internal fun tr(source: String, vararg args: Any?): String = Translations.format(Translations.strings[source] ?: source, args)

internal fun resolveLanguage(requested: String, system: Locale, supported: List<String>): String {
    if (requested in supported) return requested
    if (system.toLanguageTag() in supported) return system.toLanguageTag()
    if (system.language == "zh") return if (system.script == "Hant" || system.country in listOf("TW", "HK", "MO")) "zh-Hant" else "zh-Hans"
    return supported.firstOrNull { Locale.forLanguageTag(it).language == system.language } ?: "en"
}

@Composable
internal fun AppLanguage(language: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val supported = remember { context.assets.open("i18n/languages.json").bufferedReader().use {
        val array = JSONArray(it.readText()); (0 until array.length()).map(array::getString)
    } }
    val selected = resolveLanguage(language, config.locales[0], supported)
    val locale = remember(selected) { Locale.forLanguageTag(selected) }
    val strings = remember(selected) {
        if (selected == "en") emptyMap() else context.assets.open("i18n/$selected.json").bufferedReader().use {
            val json = JSONObject(it.readText()); json.keys().asSequence().associateWith(json::getString)
        }
    }
    val localized = remember(context, selected, config) {
        val resources = context.createConfigurationContext(Configuration(config).apply {
            setLocales(LocaleList(locale)); setLayoutDirection(locale)
        }).resources
        // Keep the Activity in the wrapper chain so attachment pickers and back handlers
        // can still find their ActivityResultRegistryOwner / OnBackPressedDispatcherOwner.
        object : ContextWrapper(context) {
            override fun getResources(): Resources = resources
        }
    }
    SideEffect { Translations.strings = strings; Locale.setDefault(locale) }
    CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides localized.resources.configuration,
        LocalLayoutDirection provides if (localized.resources.configuration.layoutDirection == 1) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) { content() }
}
