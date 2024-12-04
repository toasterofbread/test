package dev.toastbits.lifelog.extension.gdocs.alert

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.extension.ExtensionId

sealed interface GDocsLogParseAlert: LogParseAlert {
    data class MediaExtensionNotPresent(override val alertExtensionId: ExtensionId?): GDocsLogParseAlert, LogParseAlert.Warning
    data class MediaReferenceTypeNotPresent(override val alertExtensionId: ExtensionId?): GDocsLogParseAlert, LogParseAlert.Warning
}
