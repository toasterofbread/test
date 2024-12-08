package dev.toastbits.lifelog.application.dbsource.domain.model

import okio.Path

data class Alert(
    val message: String,
    val severity: Severity,
    val filePath: Path? = null,
    val lineIndex: UInt? = null
) {
    enum class Severity {
        ERROR,
        WARNING,
        UNKNOWN
    }
}
