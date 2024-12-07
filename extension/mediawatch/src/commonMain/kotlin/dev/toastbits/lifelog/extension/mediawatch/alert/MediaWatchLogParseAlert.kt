package dev.toastbits.lifelog.extension.mediawatch.alert

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension

sealed interface MediaWatchLogParseAlert: LogParseAlert {
    override val alertExtensionId: ExtensionId?
        get() = MediaWatchExtension.ID

    data class UnknownIterationSpecifier(val text: String): MediaWatchLogParseAlert, LogParseAlert.Warning
    data class UnknownDurationFormat(val text: String): MediaWatchLogParseAlert, LogParseAlert.Warning
    data class UnknownBookReadPoint(val text: String, val originalText: String): MediaWatchLogParseAlert, LogParseAlert.Warning
    data class URLInMediaTitle(val title: String): MediaWatchLogParseAlert, LogParseAlert.Error
}
