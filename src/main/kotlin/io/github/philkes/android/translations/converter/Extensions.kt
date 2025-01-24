package io.github.philkes.android.translations.converter


fun String.escapeForStringsXml(): String{
    return replace(Regex("(?<!\\\\)@"), "\\\\@")
        .replace(Regex("(?<!\\\\)\\?"), "\\\\?")
        .replace(Regex("(?<!\\\\)\\n"), "\\\\n")
        .replace(Regex("(?<!\\\\)\\t"), "\\\\t")
        .replace(Regex("(?<!\\\\)'"), "\\\\'")
        .replace(Regex("(?<!\\\\)\""), "\\\\\"")
        // Unicode characters are automatically converted by Apache POI
}

fun String.unescapeForStringsXml(): String {
    return this
        .replace("\\@", "@")
        .replace("\\?", "?")
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("\\'", "'")
        .replace("\\\"", "\"")
}
