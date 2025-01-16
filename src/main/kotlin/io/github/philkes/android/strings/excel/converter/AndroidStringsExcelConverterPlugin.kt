package io.github.philkes.android.strings.excel.converter

import io.github.philkes.android.strings.excel.converter.export.ExportToExcelTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register



class AndroidStringsExcelConverterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register<ExportToExcelTask>(EXPORT_TASK_NAME)
        project.tasks.register<ImportFromExcelTask>(IMPORT_TASK_NAME)
    }

    companion object{
         const val EXPORT_TASK_NAME = "exportTranslationsToExcel"
         const val IMPORT_TASK_NAME = "importTranslationsFromExcel"
    }
}

