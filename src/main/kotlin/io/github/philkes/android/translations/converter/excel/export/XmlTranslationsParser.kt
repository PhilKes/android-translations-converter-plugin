package io.github.philkes.android.translations.converter.excel.export

import io.github.philkes.android.translations.converter.*
import org.apache.commons.lang3.StringEscapeUtils
import org.gradle.internal.logging.progress.ProgressLogger
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


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
                        val text = element.plainValue()
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
            progressLogger.progress("Parsed ${idx+1} of ${sortedFiles.size} xml files")
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

    /**
     * Extracts plain text without escaping or omitting inner HTML/XML tags.
     */
    private fun Node.plainValue(): String {
        val stringWriter = StringWriter()
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            setOutputProperty(OutputKeys.INDENT, "no")
            setOutputProperty(OutputKeys.ENCODING, "utf-8")
        }
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            transformer.transform(DOMSource(child), StreamResult(stringWriter))
        }
        return StringEscapeUtils.unescapeHtml4(stringWriter.toString())
    }

}