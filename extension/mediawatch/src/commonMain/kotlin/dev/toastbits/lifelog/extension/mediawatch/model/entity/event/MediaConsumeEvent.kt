package dev.toastbits.lifelog.extension.mediawatch.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import dev.toastbits.lifelog.core.specification.model.string.StringId
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.localisation.MediaStringId
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

sealed interface MediaConsumeEvent: LogEvent {
    val mediaEntityType: MediaEntityType

    val mediaReference: MediaReference
    val iteration: Int?
    val iterationsUnsure: Boolean

    fun generateMediaRangeMetadata(
        strings: MediaWatchExtensionStrings,
        logStrings: LogFileConverterStrings
    ): String?

    override val typeName: StringId
        get() = MediaStringId.MediaEntityType(mediaEntityType)

    override val typeVerb: StringId
        get() = MediaStringId.MediaEntityTypeVerb(mediaEntityType)

    override suspend fun getPreview(locale: String): LogDisplayText =
        LogDisplayText.OfString(mediaReference.path.segments.lastOrNull().orEmpty())

    override fun copy(inlineComment: UserContent?, aboveComment: UserContent?): MediaConsumeEvent

    override fun copy(content: UserContent?): MediaConsumeEvent

    fun copy(
        mediaReference: MediaReference,
        iteration: Int?,
        iterationsUnsure: Boolean
    ): MediaConsumeEvent

    override fun getCompanion(): LogEntityCompanion<out MediaConsumeEvent> = Companion

    companion object: LogEntityCompanion<MediaConsumeEvent>(LogEvent) {
        override fun getProperties(): List<LogEntityProperty<MediaConsumeEvent, *>> =
            listOf(
                MediaStringId.Property.MediaConsumeEvent.MEDIA_REFERENCE.entityReferenceProperty({ mediaReference }, { copy(it as MediaReference, iteration, iterationsUnsure) }),
                MediaStringId.Property.MediaEntity.ITERATION.intProperty({ iteration }, { copy(mediaReference, it, iterationsUnsure) }, 0 .. Int.MAX_VALUE)
            )
    }
}
