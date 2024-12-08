package dev.toastbits.lifelog.core.specification.converter.alert

import dev.toastbits.lifelog.core.specification.extension.ExtensionId

sealed interface LogConvertAlert {
    val severity: Severity
    val alertExtensionId: ExtensionId?

    enum class Severity {
        ERROR, WARNING
    }
}