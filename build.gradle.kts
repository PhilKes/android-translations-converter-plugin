import io.github.philkes.android.strings.excel.converter.export.ExportToExcelTask

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.20"
    `java-gradle-plugin`
    `maven-publish`
    id("io.github.philkes.android-strings-excel-converter") version "1.0.0"
}

group = "io.github.philkes"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.named<ExportToExcelTask>("exportTranslationsToExcel") {
    inputDirectory = file("/home/user/AndroidStudioProjects/tools/android-strings-excel-converter/src/test/resources/app/src/main/res")
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("org.apache.poi:poi:5.4.0")
    implementation("org.apache.poi:poi-ooxml:5.4.0")
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
    plugins {
        register("androidStringsExcelConverter") {
            id = "io.github.philkes.android-strings-excel-converter"
            implementationClass = "io.github.philkes.android.strings.excel.converter.AndroidStringsExcelConverterPlugin"
        }
    }
}


publishing {
    repositories {
        mavenLocal()
    }
}