package dev.toastbits.lifelog.extension.gdocs

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import dev.toastbits.lifelog.extension.gdocs.impl.MediaExtensionStringsImpl
import dev.toastbits.lifelog.extension.gdocs.model.reference.MediaReferenceType

class MediaExtension(
    val strings: MediaExtensionStrings = MediaExtensionStringsImpl(),
    mediaReferenceType: MediaReferenceType = MediaReferenceType(strings)
): SpecificationExtension {
    override val id: ExtensionId get() = strings.extensionId

    override val extraEventTypes: List<LogEventType> = emptyList()

    override val extraInLogReferenceTypes: List<LogEntityReferenceType.InLog> =
        listOf(
            mediaReferenceType
        )
}
