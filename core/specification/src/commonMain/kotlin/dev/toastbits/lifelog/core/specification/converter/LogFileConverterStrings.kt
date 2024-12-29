package dev.toastbits.lifelog.core.specification.converter

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings.Companion.ILLEGAL_PATH_CHARS

interface LogFileConverterStrings {
    val metadataDirectoryName: String
    val extensionContentDirectoryName: String
    val logsDirectoryName: String
    val logFileName: String

    val contentIndentation: String
    val datePrefix: String
    val commentPrefixes: List<String>
    val ambiguousDatePrefix: String

    val blockCommentStart: String
    val blockCommentEnd: String

    val eventMetadataStart: String
    val eventMetadataEnd: String
    val eventContentStart: String
    val eventContentEnd: String

    val preferredDateFormat: LogDateFormat
    val dateFormats: List<LogDateFormat>

    fun numberToIteration(number: Int): String

    companion object {
        const val ILLEGAL_PATH_CHARS: String = "/"
    }
}


fun LogFileConverterStrings.validate() {
    metadataDirectoryName.checkPath("metadataDirectoryName")
    extensionContentDirectoryName.checkPath("extensionDirectoryName")
    check(contentIndentation.isNotEmpty())
}

private fun String.checkPath(name: String) {
    check(isNotBlank()) { "Path $name is blank" }
    check(ILLEGAL_PATH_CHARS.none { this@checkPath.contains(it) }) { "Path $name '$this' contains illegal character(s)" }
}
