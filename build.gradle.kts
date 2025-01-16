plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.20"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.0"
}

group = "io.github.philkes"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("org.apache.commons:commons-text:1.13.0")
    // uses commons-compress:1.12, higher version incompatible with some android projects
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    website = "https://github.com/PhilKes/android-translations-converter-plugin"
    vcsUrl = "https://github.com/PhilKes/android-translations-converter-plugin"
    description = "Easily convert Android strings.xml files to Excel and back"
    plugins {
        create("androidTranslationsConverter") {
            id = "io.github.philkes.android-translations-converter"
            displayName = "Android Translations Converter"
            description = "Easily convert Android translations to Excel and back"
            tags = listOf("android", "translation", "xml", "excel", "converter")
            implementationClass = "io.github.philkes.android.translations.converter.AndroidTranslationsConverterPlugin"
        }
    }
}