package io.github.philkes.android.translations.converter.excel.export

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.Color
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private const val DEFAULT_FONT = "Calibri"

fun XSSFWorkbook.saveReproducible(outputFile: File) {
    properties.coreProperties.setCreated(Optional.empty())
    properties.coreProperties.setModified(Optional.empty())
    properties.coreProperties.creator = "AndroidTranslationsConverter"

    val tempDir = Files.createTempDirectory("android-translations").toFile()
    val tempXlsxFile = tempDir.resolve("translations.xlsx")
    write(tempXlsxFile.outputStream())
    close()

    val unzippedDir = tempDir.resolve("unzipped")
    unzipFile(tempXlsxFile, unzippedDir)

    outputFile.parentFile?.mkdirs()
    createReproducibleZip(unzippedDir, outputFile)

    tempDir.deleteRecursively()
}


fun Workbook.createDefaultCellStyle(): CellStyle {
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

fun CellStyle.withColor(value: Short): CellStyle {
    fillForegroundColor = value
    fillPattern = FillPatternType.SOLID_FOREGROUND
    return this
}

fun String.toColor(): XSSFColor = XSSFColor(Color.decode("#$this"), null)

fun Sheet.addComment(
    cell: Cell,
    commentText: String
) {
    val factory = workbook.creationHelper
    val anchor = factory.createClientAnchor()
    val drawing = createDrawingPatriarch()
    val comment = drawing.createCellComment(anchor)
    comment.string = factory.createRichTextString(commentText).apply {
        val font = workbook.createFont().apply {
            fontName = DEFAULT_FONT
            fontHeightInPoints = 12
        }
        applyFont(font)
    }
    cell.cellComment = comment
}


private fun createReproducibleZip(inputFolder: File, outputFile: File) {
    ZipOutputStream(outputFile.outputStream()).use { zipOut ->
        val files = inputFolder.walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.relativeTo(inputFolder).path } // Sort files for consistent order

        for (file in files) {
            val relativePath = file.relativeTo(inputFolder).path
            val zipEntry = ZipEntry(relativePath)
            zipEntry.time = 0 // Normalize timestamp
            zipOut.putNextEntry(zipEntry)

            file.inputStream().use { input ->
                input.copyTo(zipOut)
            }

            zipOut.closeEntry()
        }
    }
}

private fun unzipFile(zipFile: File, outputFolder: File) {
    ZipFile(zipFile).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            val entryFile = File(outputFolder, entry.name)
            if (entry.isDirectory) {
                entryFile.mkdirs()
            } else {
                entryFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    entryFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}