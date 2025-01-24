package io.github.philkes.android.translations.converter.excel

import io.github.philkes.android.translations.converter.escapeForStringsXml
import io.github.philkes.android.translations.converter.unescapeForStringsXml
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun testEscapeForStringsXml() {
        val text = "This \"contains a lot\nof 'special' ch@racters?"

        val escaped = text.escapeForStringsXml()

        assertEquals("This \\\"contains a lot\\nof \\'special\\' ch\\@racters\\?", escaped)
    }

    @Test
    fun testEscapeForStringsXmlAlreadyEscaped() {
        val text = "This \\\"contains a lot\\nof \\'special\\' ch\\@racters\\?"

        val escaped = text.escapeForStringsXml()

        assertEquals("This \\\"contains a lot\\nof \\'special\\' ch\\@racters\\?", escaped)
    }

    @Test
    fun testUnscapeForStringsXml() {
        val text = "This \\\"contains a lot\\nof \\'special\\' ch\\@racters\\?"

        val escaped = text.unescapeForStringsXml()

        assertEquals("This \"contains a lot\nof 'special' ch@racters?", escaped)
    }

    @Test
    fun testUnecapeForStringsXmlAlreadyUnescaped() {
        val text = "This \"contains a lot\nof 'special' ch@racters?"

        val escaped = text.unescapeForStringsXml()

        assertEquals("This \"contains a lot\nof 'special' ch@racters?", escaped)
    }
}