@echo off
echo Running Gradle task: exportTranslationsToExcel

gradlew exportTranslationsToExcel

if %errorlevel% neq 0 (
    echo Gradle task 'exportTranslationsToExcel' failed. Please fix the issues.
    exit /b 1
)

echo Gradle task 'exportTranslationsToExcel' completed successfully.
