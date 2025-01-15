package io.github.philkes.android.strings.excel.converter


typealias TranslationKey = String
typealias LanguageFolderName = String
typealias Translation = String?

data class AndroidTranslation(val values: MutableMap<LanguageFolderName, Translation>,
                              var isTranslatable: Boolean)

data class AndroidTranslations(val translations: MutableMap<TranslationKey, AndroidTranslation>,
                               val languageFolders: MutableSet<LanguageFolderName>)