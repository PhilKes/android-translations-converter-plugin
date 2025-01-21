package io.github.philkes.android.translations.converter.excel.export

import org.apache.commons.io.FileUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class XSSFWorkbookExtensionsKtTest{
    @TempDir
    lateinit var testDir: File

    @Test
    fun testSaveReproducible() {
        val file1 = testDir.resolve("file1.xlsx")
        generateWorkbook().saveReproducible(file1)
        val file2 = testDir.resolve("file2.xlsx")
        generateWorkbook().saveReproducible(file2)

        assertTrue(FileUtils.contentEquals(file1, file2), "files do not match")
    }

    private fun generateWorkbook(): XSSFWorkbook{
       return XSSFWorkbook().apply {
            createSheet("Sheet1").let { sheet ->
                for(row in 1..5){
                    sheet.createRow(row).let { row ->
                        for(col in 1..5) {
                            row.createCell(col).let { cell ->
                                cell.setCellValue("Test Cell")
                                (cell as Cell).cellStyle =
                                    createDefaultCellStyle().withColor(IndexedColors.CORAL.index)
                                sheet.addComment(cell, "This is a comment")
                            }
                        }
                    }
                }
            }
        }
    }
}