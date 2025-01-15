package io.github.philkes.android.strings.excel.converter

import org.gradle.api.Plugin
import org.gradle.api.Project


class GreetingPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions
            .create("greeting", GreetingPluginExtension::class.java)

        project.task("hello")
            .doLast {
                println(
                    "I have a message for You: " + extension.message
                )
            }
    }
}