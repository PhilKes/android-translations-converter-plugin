package io.github.philkes.android.strings.excel.converter.export

import io.github.philkes.android.strings.excel.converter.AndroidTranslation
import io.github.philkes.android.strings.excel.converter.AndroidTranslations
import io.github.philkes.android.strings.excel.converter.PLURALS_KEY_MARKER
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.internal.logging.progress.ProgressLogger
import java.awt.Color
import java.io.File


class TranslationsExcelExporter(
    private val progressLogger: ProgressLogger,
) {

    fun exportToFile(translations: AndroidTranslations, outputFile: File, formatExcel: Boolean) {
        progressLogger.start(
            "Exporting ${translations.translations.size} translations to $outputFile",
            "Exporting..."
        )
        val workbook: Workbook = XSSFWorkbook()
        val sheet = createExcelSheet(translations, workbook)
        if (formatExcel) {
            formatSheet(sheet, translations)
        }
        outputFile.parentFile?.mkdirs()
        workbook.write(outputFile.outputStream())
        workbook.close()
        progressLogger.completed()
    }

    private fun createExcelSheet(translations: AndroidTranslations, workbook: Workbook): Sheet {
        val sheet: Sheet = workbook.createSheet(WORKBOOK_NAME)
        val headerRow = sheet.createRow(0)
        val header =
            arrayOf(KEY_COLUMN, TRANSLATABLE_COLUMN) + translations.languageFolders.toTypedArray()
        header.forEachIndexed { index, name ->
            headerRow.createCell(index).setCellValue(name)
        }

        translations.translations.entries.withIndex().forEach { (index, entry) ->
            val (key, translation) = entry
            val row = sheet.createRow(index + 1)
            var colIndex = 0

            row.createCell(colIndex++).setCellValue(key)
            row.createCell(colIndex++).setCellValue(translation.isTranslatable.toString())
            translations.languageFolders.forEach { folderName ->
                row.createCell(colIndex++).setCellValue(translation.values[folderName] ?: "")
            }
            progressLogger.progress("Processed ${index+1} of ${translations.translations.size} translations")
        }
        return sheet
    }

    private fun formatSheet(sheet: Sheet, translations: AndroidTranslations) {
        progressLogger.progress("Formatting Excel Sheet...")
        val workbook = sheet.workbook
        val defaultCellStyle = workbook.createDefaultCellStyle()
        val emptyCellStyle = workbook.createDefaultCellStyle().withColor(EMPTY_CELL_COLOR)
        val emptyPluralsCellStyle =
            workbook.createDefaultCellStyle().withColor(EMPTY_PLURALS_CELL_COLOR)
        val hiddenCellStyle = workbook.createDefaultCellStyle().withColor(EMPTY_HIDDEN_CELL_COLOR)
        val headerCellStyle = workbook.createDefaultCellStyle().apply {
            val font = sheet.workbook.createFont().apply {
                fontName = DEFAULT_FONT
                bold = true
            }
            setFont(font)
            setFillForegroundColor(EMPTY_HIDDEN_CELL_COLOR.toColor())
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        sheet.apply {
            defaultColumnWidth = DEFAULT_COLUMN_CHARACTER_WIDTH
            setColumnWidth(0, KEY_COLUMN_CHARACTER_WIDTH * 256)
            setColumnHidden(1, true) // Translatable column
            forEach { row ->
                if (row.rowNum == 0) {
                    row.forEach { it.cellStyle = headerCellStyle }
                    return@forEach
                }
                val key = row.first().stringCellValue
                val isPluralKeyWithNoDefaultValue = key.contains(PLURALS_KEY_MARKER)
                        && row.getCell(DEFAULT_LANGUAGE_COLUMN_IDX).stringCellValue.isBlank()
                row.forEach { cell ->
                    formatCell(
                        cell,
                        isPluralKeyWithNoDefaultValue,
                        sheet,
                        key,
                        emptyPluralsCellStyle,
                        emptyCellStyle,
                        defaultCellStyle
                    )
                }
            }

            translations.translations.values.filter { !it.isTranslatable }
                .forEach { hideTranslation(translations, it, sheet, hiddenCellStyle) }

        }
        sheet.createFreezePane(3, 1)
        sheet.setAutoFilter(
            CellRangeAddress(
                0, translations.translations.size,
                0, translations.languageFolders.size + 2
            )
        )
    }

    private fun Sheet.hideTranslation(
        translations: AndroidTranslations,
        translation: AndroidTranslation,
        sheet: Sheet,
        hiddenCellStyle: CellStyle
    ) {
        val rowIdx = translations.translations.values.indexOf(translation)
        getRow(rowIdx + 1)?.let { row ->
            row.forEach { cell ->
                if (cell.columnIndex == DEFAULT_LANGUAGE_COLUMN_IDX) {
                    cell.addComment(
                        sheet,
                        "This key is marked as not-translatable, do not add translations in this row."
                    )
                }
                cell.cellStyle = hiddenCellStyle
            }
            row.zeroHeight = true
        }
    }

    private fun formatCell(
        cell: Cell,
        isPluralKeyWithNoDefaultValue: Boolean,
        sheet: Sheet,
        key: String,
        emptyPluralsCellStyle: CellStyle,
        emptyCellStyle: CellStyle,
        defaultCellStyle: CellStyle
    ) {
        cell.apply {
            if (isPluralKeyWithNoDefaultValue) {
                addComment(
                    sheet,
                    "The default language does not need/have a translation for the quantity '${
                        key.split(PLURALS_KEY_MARKER)[1]
                    }'.\nIf this language needs a different translation for this quantity, add it, otherwise ignore this row."
                )
            }
            if (cell.stringCellValue.isBlank()) {
                cellStyle = if (isPluralKeyWithNoDefaultValue) emptyPluralsCellStyle
                else emptyCellStyle
            } else {
                cellStyle = defaultCellStyle
            }
        }
    }

    private fun Workbook.createDefaultCellStyle(): CellStyle {
        return createCellStyle().apply {
            val font = createFont()
            font.fontName = DEFAULT_FONT
            setFont(font)
            dataFormat = createDataFormat().getFormat("@")
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            wrapText = true
        }
    }

    private fun CellStyle.withColor(value: String): CellStyle {
        setFillForegroundColor(value.toColor())
        fillPattern = FillPatternType.SOLID_FOREGROUND
        return this
    }

    private fun String.toColor(): XSSFColor = XSSFColor(Color.decode("#$this"), null)

    private fun Cell.addComment(
        sheet: Sheet,
        commentText: String
    ) {
        val factory = sheet.workbook.creationHelper
        val anchor = factory.createClientAnchor()
        val drawing = sheet.createDrawingPatriarch()
        val comment = drawing.createCellComment(anchor)
        comment.string = factory.createRichTextString(commentText).apply {
            val font = sheet.workbook.createFont().apply {
                fontName = DEFAULT_FONT
                fontHeightInPoints = 12
            }
            applyFont(font)
        }
        cellComment = comment
    }

    companion object {
        private const val DEFAULT_LANGUAGE_COLUMN_IDX = 2

        private const val DEFAULT_FONT = "Calibri"
        private const val WORKBOOK_NAME = "Translations"

        private const val KEY_COLUMN = "Key"
        private const val TRANSLATABLE_COLUMN = "Translatable"

        private const val EMPTY_CELL_COLOR = "FFCCCC"
        private const val EMPTY_PLURALS_CELL_COLOR = "ffffcc"
        private const val EMPTY_HIDDEN_CELL_COLOR = "D3D3D3"

        private const val DEFAULT_COLUMN_CHARACTER_WIDTH = 50
        private const val KEY_COLUMN_CHARACTER_WIDTH = 40
    }
}
