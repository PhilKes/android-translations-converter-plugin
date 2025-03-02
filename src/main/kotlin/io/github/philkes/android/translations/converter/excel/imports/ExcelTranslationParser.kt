package io.github.philkes.android.translations.converter.excel.imports

import io.github.philkes.android.translations.converter.AndroidTranslation
import io.github.philkes.android.translations.converter.AndroidTranslations
import io.github.philkes.android.translations.converter.LanguageFolderName
import io.github.philkes.android.translations.converter.TranslationKey
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.gradle.internal.logging.progress.ProgressLogger
import java.io.File

class ExcelTranslationParser(private val progressLogger: ProgressLogger) {

    fun parse(inputFile: File): AndroidTranslations {
        ZipSecureFile.setMinInflateRatio(0.0)
        val workbook = WorkbookFactory.create(inputFile)
        val sheet = workbook.getSheetAt(0)

        val translations = mutableMapOf<TranslationKey, AndroidTranslation>()
        val languageFolders = mutableSetOf<LanguageFolderName>()

        progressLogger.start("Parsing translations from Excel file", "Parsing...")

        val headerRow = sheet.getRow(0)

        for (i in 2 until headerRow.lastCellNum) {
            languageFolders.add(headerRow.getCell(i).stringCellValue)
        }

        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex)
            val keyCell = row.getCell(0) ?: continue
            val translatableCell = row.getCell(1)

            val key = keyCell.stringCellValue
            val translatable = translatableCell.stringCellValue?.toBooleanStrictOrNull() ?: true

            val translation = AndroidTranslation(mutableMapOf(), translatable)

            for (i in 2 until headerRow.lastCellNum) {
                val language = headerRow.getCell(i).stringCellValue
                val translationCell = row.getCell(i)
                val translationValue = translationCell?.stringCellValue ?: ""
                translation.values[language] = translationValue
            }

            translations[key] = translation
            progressLogger.progress("Parsed $rowIndex of ${sheet.physicalNumberOfRows} translation keys")
        }
        progressLogger.completed()
        return AndroidTranslations(translations, languageFolders)
    }
}