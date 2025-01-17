package io.github.philkes.android.strings.excel.converter

import io.github.philkes.android.translations.converter.excel.imports.ExcelTranslationParser
import io.github.philkes.android.translations.converter.excel.imports.TranslationsXmlImporter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.kotlin.dsl.support.get
import java.io.File

/**
 * Imports Android strings.xml files from a single Excel file.
 */
open class ImportFromExcelTask : DefaultTask() {

    init {
        description = " Imports Android strings.xml files from a single Excel file."
        group = "translations"
    }

    /**
     * Input Excel File containing translations.
     * Format:
     * * The first row: Key (translation-key), 'Translatable' (true/false), folder-names/languages (e.g. 'values-de')
     * * Each other row represents one translation-key and its translations in the available languages
     * * For plurals, for every possible quantity (see [PLURALS_QUANTITIES]) there is a separate row with Key: `{KEY}_PLURALS_{QUANTITY}`
     *
     * (Defaults to `{PROJECT_DIR}/translations.xlsx`)
     */
    @InputFile
    var inputFile: File = project.file("translations.xlsx")

    /**
     * Folder to import into.
     * For every folder-name/language a subfolder will be created and its corresponding `strings.xml` generated.
     *
     * (Defaults to `{PROJECT_DIR}//src/main/res/`)
     */
    @OutputDirectory
    var outputDirectory: File = project.file("src/main/res/")

    @TaskAction
    fun import() {
        logger.lifecycle("Start importing translations from Excel (inputFile: '${inputFile.absolutePath}', outputDir: '${outputDirectory.absolutePath}')")
        val progressLoggerFactory = services.get<ProgressLoggerFactory>()

        val androidTranslations = ExcelTranslationParser(
            progressLoggerFactory.newOperation(ExcelTranslationParser::class.java)
        ).parse(inputFile)

        TranslationsXmlImporter(
            progressLoggerFactory.newOperation(TranslationsXmlImporter::class.java)
        ).import(androidTranslations, outputDirectory)

        logger.lifecycle("Finished import of ${androidTranslations.translations.size} translation-keys (from ${androidTranslations.languageFolders.size} languages) to 'strings.xml' files: $outputDirectory")
    }
}
