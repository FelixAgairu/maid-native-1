package com.hatsyrei.maidnative.ui.i18n

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class TranslationsTest {
    @Test fun `placeholder insertion does not interpret user text as more placeholders`() {
        assertEquals("A {1} B value",Translations.format("A {0} B {1}",arrayOf("{1}","value")))
    }
    @Test fun `system languages resolve regional and script variants`() {
        val supported=listOf("en","zh-Hans","zh-Hant","es","ar")
        assertEquals("zh-Hant",resolveLanguage("",Locale.forLanguageTag("zh-TW"),supported))
        assertEquals("zh-Hans",resolveLanguage("",Locale.forLanguageTag("zh-CN"),supported))
        assertEquals("es",resolveLanguage("",Locale.forLanguageTag("es-MX"),supported))
        assertEquals("ar",resolveLanguage("ar",Locale.US,supported))
        assertEquals("en",resolveLanguage("",Locale.forLanguageTag("xx"),supported))
    }
}
