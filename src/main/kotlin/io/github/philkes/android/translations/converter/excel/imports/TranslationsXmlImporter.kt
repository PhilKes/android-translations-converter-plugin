package io.github.philkes.android.translations.converter.excel.imports

import io.github.philkes.android.translations.converter.*
import org.gradle.internal.logging.progress.ProgressLogger
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TranslationsXmlImporter(private val progressLogger: ProgressLogger) {

    fun import(translations: AndroidTranslations, outputDir: File) {
        outputDir.mkdirs()
        progressLogger.start("Importing Translations from Excel File", "Importing...")
        for ((idx,language) in translations.languageFolders.withIndex()) {
            val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
            val rootElement = xmlDoc.createElement(RESOURCES_XML_TAG)
            xmlDoc.appendChild(rootElement)

            val translationsForLanguage = translations.translations.filter { (_, translation) ->
                translation.values.containsKey(language)
            }

            for ((key, androidTranslation) in translationsForLanguage.entries.sortedBy { it.key }) {
                androidTranslation.values[language]?.let { translationValue ->
                    if (translationValue.isNotEmpty()) {
                        val escapedValue = translationValue.escapeForStringsXml()
                        if (key.contains(PLURALS_KEY_MARKER)) {
                            val (actualKey, quantity) = key.split(PLURALS_KEY_MARKER)
                            handlePlurals(rootElement, actualKey, escapedValue, quantity)
                        } else {
                            handleRegularString(
                                xmlDoc,
                                key,
                                escapedValue,
                                androidTranslation.isTranslatable,
                                rootElement
                            )
                        }
                    }
                }

            }

            writeXmlToFile(xmlDoc, language, outputDir)
            progressLogger.progress("Imported ${idx+1} of ${translations.languageFolders.size} languages from Excel File")
        }
        progressLogger.completed()
    }

    private fun handlePlurals(
        root: org.w3c.dom.Element,
        stringName: String,
        stringValue: String,
        quantity: String
    ) {
        // Find an existing <plurals> element with the same name
        val pluralGroupElem = findPluralElement(root, stringName)

        // If not found, create a new <plurals> element
        val group = pluralGroupElem ?: root.ownerDocument.createElement(PLURALS_XML_TAG).apply {
            setAttribute(NAME_XML_ATTRIBUTE, stringName)
            root.appendChild(this)
        }

        // Add the <item> element to the <plurals> group
        val itemElem = root.ownerDocument.createElement(ITEM_XML_TAG).apply {
            setAttribute(QUANTITY_XML_ATTRIBUTE, quantity)
            textContent = stringValue
        }
        group.appendChild(itemElem)
    }

    private fun findPluralElement(root: org.w3c.dom.Element, stringName: String): org.w3c.dom.Element? {
        val nodeList = root.getElementsByTagName(PLURALS_XML_TAG)
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node is org.w3c.dom.Element && node.getAttribute(NAME_XML_ATTRIBUTE) == stringName) {
                return node
            }
        }
        return null
    }


    private fun handleRegularString(doc: org.w3c.dom.Document, key: String, value: String, translatable: Boolean, rootElement: org.w3c.dom.Element) {
        val stringElement = doc.createElement(STRING_XML_TAG)
        stringElement.setAttribute(NAME_XML_ATTRIBUTE, key)

        if (!translatable) {
            stringElement.setAttribute(TRANSLATABLE_XML_ATTRIBUTE, "false")
        }

        stringElement.textContent = value
        rootElement.appendChild(stringElement)
    }

    private fun writeXmlToFile(doc: org.w3c.dom.Document, language: String, outputDir: File) {
        val outputLanguageDir = File(outputDir, language)
        if (!outputLanguageDir.exists()) {
            outputLanguageDir.mkdirs()
        }

        val outputFile = File(outputLanguageDir, TRANSLATIONS_FILE_NAME)
        val transformerFactory = TransformerFactory.newInstance()

        // Configure transformer for pretty-printing
        val transformer = transformerFactory.newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.METHOD, "xml")
            setOutputProperty(OutputKeys.ENCODING, "utf-8")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
            setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ""); // Leads to new line after XML declaration
        }
        doc.xmlStandalone = true
        transformer.transform(DOMSource(doc), StreamResult(outputFile))
    }

    companion object{
        const val TRANSLATIONS_FILE_NAME = "strings.xml"
    }
}
