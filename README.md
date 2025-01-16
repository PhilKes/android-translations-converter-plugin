# AndroidStringsExcelConverter

Plug'n'Play gradle plugin for your Android projects to convert between Android `strings.xml` translations and Excel.
Useful if your translations are created by non-technical/external translators who prefer to use Excel sheets.

## Features

* Export from android project's `strings.xml` files to a single, formatted Excel File
* Import `strings.xml` files from given Excel File into corresponding `values` subfolders
* Supports [Android quantity strings (plurals)](https://developer.android.com/guide/topics/resources/string-resource#Plurals)

## Setup

In `build.gradle`:
```groovy
plugins {
    id 'io.github.philkes.android-strings-excel-converter'
}
```

## Export

Configuration in `build.gradle` (shown values are the defaults):
```groovy
tasks.named("exportTranslationsToExcel", ExportToExcelTask) {
    /**
     * Input path which contains 'strings.xml' (or in subfolders)
     */
    inputDirectory = project.file("app/src/main/res")
    
    /**
     * Exported Excel file path (.xlsx)
     */
    outputFile = project.file("translations.xlsx")

   /**
    * Whether the exported Excel sheet should be formatted.
    * If set to `true`, this will:
    * * Highlight missing translations in light red
    * * Hide non-translatable keys/rows from the user
    * * Add helpful comments to plural quantity keys
    * * Add Auto-Filters to every column header for easy filtering for e.g. all missing translations
    * * Freeze the Key and default language ('values') columns
    */
    formatExcel = true
}
```

Run `./gradlew exportTranslationsToExcel`

### Example Excel

This is an example exported Excel Sheet:
<img src="./doc/example_excel.png" alt="example-excel" /> 

To preview a full exported Excel file [click here](https://github.com/PhilKes/android-strings-excel-converter/raw/refs/heads/main/src/test/resources/expected.xlsx)

#### Plurals

Android supports [quantity strings (plurals)](https://developer.android.com/guide/topics/resources/string-resource#Plurals).
To support these plurals, for every `<plurals>` in the `strings.xml` there are multiple for all of the supported quantities.
The keys for these plurals have appended `_PLURALS_{QUANTITY}` to differentiate them. There is always a row for every possible quantity, doesn't matter if there is an existing translation in a language or not. If the default language does not specify a translations for a certain quantity, this row is highlighted in yellow with a corresponding comment.


## Import

Configuration in `build.gradle` (shown values are the defaults):
```groovy
tasks.named("importTranslationsFromExcel", ImportFromExcelTask) {
   /**
    * Input Excel File containing translations.
    * Format:
    * * The first row: Key (translation-key), 'Translatable' (true/false), folder-names/languages (e.g. 'values-de')
    * * Each other row represents one translation-key and its translations in the available languages
    * * For plurals, for every possible quantity (see [PLURALS_QUANTITIES]) there is a separate row with Key: `{KEY}_PLURALS_{QUANTITY}`
    */
    inputFile = project.file("translations.xlsx")

   /**
    * Folder to import to.
    * For every folder-name/language a subfolder will be created and its corresponding `strings.xml` generated.
    */
    outputFile = project.file("app/src/main/res/")
}
```

Run `./gradlew importTranslationsFromExcel`

To preview the output see [app/src/main/res](./src/test/resources/app/src/main/res) folder

 ---

### Automate Export via pre-commit Hook

The `exportTranslationsToExcel` can easily be automated, in order to always have the exported `translation.xlsx` up-to-date with the Android translations.
This can be done either via a pre-commit hook or by simply executing it on every build.

_Note that `exportTranslationsToExcel` execution is skipped if none of the `strings.xml` files contents have changed since the last execution._

#### Execute on build automatically

In `build.gradle`:
```groovy
preBuild.dependsOn exportTranslationsToExcel
```

#### Execute as pre-commit hook
1. Copy [pre-commit folder](./pre-commit) to the root of your project
2. In `build.gradle`:
    ```groovy
    tasks.register('installLocalGitHooks', Copy) {
        def scriptsDir = new File(rootProject.rootDir, 'scripts/')
        def hooksDir = new File(rootProject.rootDir, '.git/hooks')
        from(scriptsDir) {
            include 'pre-commit', 'pre-commit.bat'
        }
        into { hooksDir }
        inputs.files(file("${scriptsDir}/pre-commit"), file("${scriptsDir}/pre-commit.bat"))
        outputs.dir(hooksDir)
        fileMode 0775
    }
    preBuild.dependsOn installLocalGitHooks
    ```
