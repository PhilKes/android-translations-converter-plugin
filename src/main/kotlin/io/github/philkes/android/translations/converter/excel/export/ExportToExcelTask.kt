package io.github.philkes.android.translations.converter.excel.export

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.kotlin.dsl.support.get
import java.io.File

/**
 * Exports Android strings.xml files to a single Excel file.
 */
abstract class ExportToExcelTask : org.gradle.api.DefaultTask() {

    init {
        description = "Exports Android strings.xml files to a single Excel file"
        group = "translations"
    }

    /**
     * Input path which contains 'strings.xml' (or in subfolders).
     *
     * (Defaults to `{PROJECT_DIR}/app/src/main/res`)
     */
    @InputDirectory
    var inputDirectory: File = project.file("app/src/main/res")

    /**
     * The task is invalidated if any `strings.xml` files contents in the [inputDirectory] and its subfolders change.
     */
    @InputFiles
    fun getInputFiles(): FileCollection {
        return project.fileTree(inputDirectory).apply {
            include("**/strings.xml")
        }
    }

    /**
     * Exported Excel file path (.xlsx).
     *
     * (Defaults to `{PROJECT_DIR}/translations.xlsx`)
     */
    @OutputFile
    var outputFile: File = project.file("translations.xlsx")

    /**
     * Whether the exported Excel sheet should be formatted.
     * If set to `true`, this will:
     * * Highlight missing translations in light red
     * * Hide non-translatable keys/rows from the user
     * * Add helpful comments to plural quantity keys
     * * Add Auto-Filters to every column header for easy filtering for e.g. all missing translations
     * * Freeze the Key and default language ('values') columns
     *
     * (Defaults to `true`)
     */
    @Input
    var formatExcel: Boolean = true

    @TaskAction
    fun export() {
        logger.lifecycle("Start exporting to Excel (inputDirectory: '${inputDirectory.absolutePath}', outputFile: '${outputFile.absolutePath}' formatExcel: $formatExcel)")
        val stringsXmlFiles = getInputFiles().files
        if(stringsXmlFiles.isEmpty()) {
            logger.warn("No 'strings.xml' files were found in ${inputDirectory.absolutePath}")
        }
        val progressLoggerFactory = services.get<ProgressLoggerFactory>()

        val androidTranslations = XmlTranslationsParser(
            progressLoggerFactory.newOperation(XmlTranslationsParser::class.java)
        ).parse(stringsXmlFiles)

        TranslationsExcelExporter(
            progressLoggerFactory.newOperation(TranslationsExcelExporter::class.java)
        ).exportToFile(androidTranslations, outputFile,formatExcel)

        logger.lifecycle("Finished export of ${androidTranslations.translations.size} translation-keys (from ${stringsXmlFiles.size} 'strings.xml' files) to Excel: $outputFile")
    }
}