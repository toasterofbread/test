package dev.toastbits.lifelog.extension.gdocs.alert

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.extension.gdocs.GDocsExtension

sealed interface GDocsLogParseAlert: LogParseAlert {
    override val alertExtensionId: ExtensionId?
        get() = GDocsExtension.ID

    data object MediaExtensionNotPresent: GDocsLogParseAlert, LogParseAlert.Warning
    data object MediaReferenceTypeNotPresent: GDocsLogParseAlert, LogParseAlert.Warning
}
