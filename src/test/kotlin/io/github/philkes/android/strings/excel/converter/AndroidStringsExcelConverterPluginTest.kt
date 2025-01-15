package io.github.philkes.android.strings.excel.converter

import io.github.philkes.android.strings.excel.converter.AndroidStringsExcelConverterPlugin.Companion.EXPORT_TASK_NAME
import io.github.philkes.android.strings.excel.converter.export.ExportToExcelTask
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileInputStream


class AndroidStringsExcelConverterPluginTest {

    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        buildFile = File(testProjectDir, "build.gradle.kts")
    }

    @Test
    fun exportTranslationsToExcel() {
        val buildFileContent = """
         import ${ExportToExcelTask::class.java.name}
            
         plugins {
            id("io.github.philkes.android-strings-excel-converter")
         }
         
         tasks.named<${ExportToExcelTask::class.java.simpleName}>("$EXPORT_TASK_NAME") {
             inputDirectory = file("${javaClass.classLoader.getResource("app/src/main/res")!!.path}")
         }
      """.trimIndent()

        buildFile.writeText(buildFileContent)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(EXPORT_TASK_NAME)
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(SUCCESS, result.task(":$EXPORT_TASK_NAME")?.outcome ?: FAILED, "'$EXPORT_TASK_NAME' gradle task failed")
        val outputFile = File(testProjectDir, "translations.xlsx")
        assertTrue(outputFile.exists(), "outputFile does not exists")
        assertTrue(outputFile.length() > 0, "outputFile is empty")
        assertTrue(compareExcelFiles(File(javaClass.classLoader.getResource("expected.xlsx")!!.path),outputFile), "outputFile's contents are not as expected")

    }

    private fun compareExcelFiles(file1: File, file2: File): Boolean {
        val workbook1: Workbook = XSSFWorkbook(FileInputStream(file1))
        val workbook2: Workbook = XSSFWorkbook(FileInputStream(file2))
        try {
            if (workbook1.numberOfSheets != workbook2.numberOfSheets) {
                return false
            }
            for (sheetIndex in 0 until workbook1.numberOfSheets) {
                val sheet1 = workbook1.getSheetAt(sheetIndex)
                val sheet2 = workbook2.getSheetAt(sheetIndex)
                if (sheet1.physicalNumberOfRows != sheet2.physicalNumberOfRows) {
                    return false
                }
                for (rowIndex in 0 until sheet1.physicalNumberOfRows) {
                    val row1 = sheet1.getRow(rowIndex)
                    val row2 = sheet2.getRow(rowIndex)
                    if (row1?.physicalNumberOfCells != row2?.physicalNumberOfCells) {
                        return false
                    }
                    for (cellIndex in 0 until row1.physicalNumberOfCells) {
                        if (!row1.getCell(cellIndex).isEqualTo(row2.getCell(cellIndex))) {
                            return false
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            workbook1.close()
            workbook2.close()
        }
    }

    private fun Cell?.isEqualTo(other: Cell?)= this?.stringCellValue?.equals(other?.stringCellValue) ?: false
}