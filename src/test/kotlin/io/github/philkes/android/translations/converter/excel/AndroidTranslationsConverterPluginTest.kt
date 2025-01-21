package io.github.philkes.android.translations.converter.excel

import io.github.philkes.android.strings.excel.converter.ImportFromExcelTask
import io.github.philkes.android.translations.converter.AndroidTranslationsConverterPlugin.Companion.EXPORT_TASK_NAME
import io.github.philkes.android.translations.converter.AndroidTranslationsConverterPlugin.Companion.IMPORT_TASK_NAME
import io.github.philkes.android.translations.converter.excel.export.ExportToExcelTask
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.isRegularFile
import kotlin.io.path.name


class AndroidTranslationsConverterPluginTest {

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
            id("io.github.philkes.android-translations-converter")
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

        val outputFile = File(testProjectDir, "translations.xlsx")
        val expected = File(javaClass.classLoader.getResource("expected.xlsx")!!.path)
        assertEquals(SUCCESS, result.task(":$EXPORT_TASK_NAME")?.outcome ?: FAILED, "'$EXPORT_TASK_NAME' gradle task failed")
        assertTrue(outputFile.exists(), "outputFile does not exists")
        assertTrue(outputFile.length() > 0, "outputFile is empty")
        assertTrue(FileUtils.contentEquals(expected, outputFile), "outputFile's byte contents are not equal")
    }

    @Test
    fun importTranslationsFromExcel() {
        val outputDirectory = File(testProjectDir, "output")
        val buildFileContent = """
         import ${ImportFromExcelTask::class.java.name}

         plugins {
            id("io.github.philkes.android-translations-converter")
         }

         tasks.named<${ImportFromExcelTask::class.java.simpleName}>("$IMPORT_TASK_NAME") {
             inputFile = file("${javaClass.classLoader.getResource("expected.xlsx")!!.path}")
             outputDirectory = file("${outputDirectory.path}")
         }
      """.trimIndent()
        buildFile.writeText(buildFileContent)

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(IMPORT_TASK_NAME)
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(
            SUCCESS,
            result.task(":$IMPORT_TASK_NAME")?.outcome ?: FAILED,
            "'$IMPORT_TASK_NAME' gradle task failed"
        )
        val expectedOutputDirectory =
            File(javaClass.classLoader.getResource("app/src/main/res")!!.path).toPath()
        expectedOutputDirectory.assertContentsEqual(outputDirectory.toPath())
    }

    private fun Path.assertContentsEqual(other: Path) {
        Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
            override fun visitFile(
                path: Path,
                attrs: BasicFileAttributes
            ): FileVisitResult {
                val result: FileVisitResult = super.visitFile(path, attrs)

                val relativize: Path = this@assertContentsEqual.relativize(path)
                val otherPath: Path = other.resolve(relativize)
                assertEquals(path.name, otherPath.name, "File names differ")
                if(path.isRegularFile()){
                    assertEquals(path.parent.name, otherPath.parent.name, "File folders differ")
                    assertTrue(path.contentsDiffer(otherPath)){ path.diff(otherPath)}
                }
                return result
            }
        })
    }
}

private fun Path.contentsDiffer(otherFile: Path): Boolean{
    val bytes1 = Files.readAllBytes(this)
    val bytes2 = Files.readAllBytes(otherFile)
    return !bytes1.contentEquals(bytes2)
}

private fun Path.diff(fileInOther: Path): String {
    val string = StringBuilder("Expected '$this' to be equal to '$fileInOther':")
    var lineCounter = 0
    val input1 = Scanner(this.toFile())
    val input2 = Scanner(fileInOther.toFile())
    while (input1.hasNextLine() && input2.hasNextLine()) {
        lineCounter++
        val first = input1.nextLine()
        val second = input2.nextLine()
        if (!first.equals(second)) {
            string.append("\n\nLine $lineCounter:\nExpected:\n$first\nActual:\n$second")
        }
    }
    return string.toString()
}
