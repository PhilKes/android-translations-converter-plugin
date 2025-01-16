package io.github.philkes.android.translations.converter

import io.github.philkes.android.strings.excel.converter.ImportFromExcelTask
import io.github.philkes.android.translations.converter.excel.export.ExportToExcelTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register



class AndroidTranslationsConverterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register<ExportToExcelTask>(EXPORT_TASK_NAME)
        project.tasks.register<ImportFromExcelTask>(IMPORT_TASK_NAME)
    }

    companion object{
         const val EXPORT_TASK_NAME = "exportTranslationsToExcel"
         const val IMPORT_TASK_NAME = "importTranslationsFromExcel"
    }
}

