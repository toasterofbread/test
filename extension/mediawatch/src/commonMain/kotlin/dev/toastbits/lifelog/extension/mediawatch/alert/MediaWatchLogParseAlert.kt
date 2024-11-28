package dev.toastbits.lifelog.extension.mediawatch.alert

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.extension.ExtensionId

sealed interface MediaWatchLogParseAlert: LogParseAlert {
    data class UnknownIterationSpecifier(override val alertExtensionId: ExtensionId, val text: String): MediaWatchLogParseAlert, LogParseAlert.Warning
    data class UnknownDurationFormat(override val alertExtensionId: ExtensionId, val text: String): MediaWatchLogParseAlert, LogParseAlert.Warning
    data class UnknownBookReadPoint(override val alertExtensionId: ExtensionId, val text: String, val originalText: String): MediaWatchLogParseAlert, LogParseAlert.Warning
    data class URLInMediaTitle(override val alertExtensionId: ExtensionId, val title: String): MediaWatchLogParseAlert, LogParseAlert.Error
}
