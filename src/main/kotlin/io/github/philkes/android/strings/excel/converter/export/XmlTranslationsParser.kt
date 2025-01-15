package io.github.philkes.android.strings.excel.converter.export

import io.github.philkes.android.strings.excel.converter.AndroidTranslation
import io.github.philkes.android.strings.excel.converter.AndroidTranslations
import org.gradle.api.logging.Logger
import org.gradle.internal.logging.progress.ProgressLogger
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


class XmlTranslationsParser(private val progressLogger: ProgressLogger) {

    fun parse(stringsXmlFiles: Set<File>): AndroidTranslations {
        val sortedFiles = stringsXmlFiles.sortedBy { it.parentFile?.name }
        progressLogger.start("Parsing ${sortedFiles.size} xml files", "Parsing...")
        val translations = mutableMapOf<String, AndroidTranslation>()
        val folderNames = mutableSetOf<String>()
        for ((idx,file) in sortedFiles.withIndex()) {
            val folderName = file.parentFile.name
            folderNames.add(folderName)

            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val root = document.documentElement

            root.childNodes.let { nodeList ->
                for (i in 0 until nodeList.length) {
                    val element = nodeList.item(i)
                    if (element.nodeName == STRING_XML_TAG) {
                        val key = element.name() ?: continue
                        val text = element.textContent
                        translations.getOrPut(key) {
                            AndroidTranslation(mutableMapOf(), element.isTranslatable())
                        }.apply {
                            values[folderName] = text
                            isTranslatable = element.isTranslatable()
                        }
                    } else if (element.nodeName == PLURALS_XML_TAG) {
                        parsePlurals(
                            element as Element,
                            folderName,
                            translations,
                        )
                    }
                }
            }
            progressLogger.progress("Parsed $idx of ${sortedFiles.size} xml files")
        }
        progressLogger.completed()
        return AndroidTranslations(translations, folderNames)
    }
    private fun Node.name() = attributeValue(NAME_XML_ATTRIBUTE)
    private fun Node.isTranslatable() = attributes.getNamedItem(TRANSLATABLE_XML_ATTRIBUTE)?.let { attribute ->
        attribute.nodeValue?.let { it.isEmpty() || it == "true" } ?: true
    } ?: true

    private fun Node.attributeValue(attribute: String) = attributes.getNamedItem(attribute)?.nodeValue

    private fun parsePlurals(
        element: Element, folderName: String,
        translations: MutableMap<String, AndroidTranslation>,
    ) {
        val key = element.name()

        for (quantity in PLURALS_QUANTITIES) {
            val pluralKey = "${key}$PLURALS_KEY_MARKER${quantity}"
            val text = element.getElementsByTagName(ITEM_XML_TAG).let { itemList ->
                (0 until itemList.length).map { itemList.item(it) }
                    .find { it.attributeValue(QUANTITY_XML_ATTRIBUTE) == quantity }
                    ?.textContent
            }

            translations.getOrPut(pluralKey) {
                AndroidTranslation(mutableMapOf(),element.isTranslatable())
            }.apply {
                values[folderName] = text
                isTranslatable = element.isTranslatable()
            }
        }
    }

    companion object{
        private const val STRING_XML_TAG = "string"
        private const val PLURALS_XML_TAG = "plurals"
        private const val ITEM_XML_TAG = "item"

        private const val NAME_XML_ATTRIBUTE = "name"
        private const val TRANSLATABLE_XML_ATTRIBUTE = "translatable"
        private const val QUANTITY_XML_ATTRIBUTE = "quantity"
        private val PLURALS_QUANTITIES = listOf("few", "many", "one", "other", "two", "zero")

        const val PLURALS_KEY_MARKER = "_PLURALS_"
    }
}