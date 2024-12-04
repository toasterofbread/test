package dev.toastbits.lifelog.extension.mediawatch

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import dev.toastbits.lifelog.extension.mediawatch.impl.MediaWatchExtensionStringsImpl
import dev.toastbits.lifelog.extension.mediawatch.impl.model.entity.event.MediaConsumeEventTypeImpl
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MediaConsumeEventType
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReferenceType

class MediaWatchExtension(
    val strings: MediaWatchExtensionStrings = MediaWatchExtensionStringsImpl(),
    mediaConsumeEventType: MediaConsumeEventType = MediaConsumeEventTypeImpl(strings),
    mediaReferenceType: MediaReferenceType = MediaReferenceType(strings)
): SpecificationExtension {
    override val id: ExtensionId get() = strings.extensionId

    override val extraEventTypes: List<LogEventType> =
        listOf(
            mediaConsumeEventType
        )

    override val extraInMetadataReferenceTypes: List<LogEntityReferenceType.InMetadata> =
        listOf(
            mediaReferenceType
        )
}
