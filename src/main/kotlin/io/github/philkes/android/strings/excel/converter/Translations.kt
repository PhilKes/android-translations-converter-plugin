package io.github.philkes.android.strings.excel.converter

const val RESOURCES_XML_TAG = "resources"

const val STRING_XML_TAG = "string"
const val NAME_XML_ATTRIBUTE = "name"
const val TRANSLATABLE_XML_ATTRIBUTE = "translatable"

const val PLURALS_XML_TAG = "plurals"
const val ITEM_XML_TAG = "item"
const val PLURALS_KEY_MARKER = "_PLURALS_"
const val QUANTITY_XML_ATTRIBUTE = "quantity"
val PLURALS_QUANTITIES = listOf("few", "many", "one", "other", "two", "zero")

typealias TranslationKey = String
typealias LanguageFolderName = String
typealias Translation = String?

data class AndroidTranslation(val values: MutableMap<LanguageFolderName, Translation>,
                              var isTranslatable: Boolean)

data class AndroidTranslations(val translations: MutableMap<TranslationKey, AndroidTranslation>,
                               val languageFolders: MutableSet<LanguageFolderName>)