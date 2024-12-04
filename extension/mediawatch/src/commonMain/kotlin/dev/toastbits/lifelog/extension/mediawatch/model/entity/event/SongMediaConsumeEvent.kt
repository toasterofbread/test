package dev.toastbits.lifelog.extension.mediawatch.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.string.StringId
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

data class SongMediaConsumeEvent(
    override val extensionId: ExtensionId?,
    override var mediaReference: MediaReference,
    override var inlineComment: UserContent? = null,
    override var aboveComment: UserContent? = null,
    override var content: UserContent? = null,
    override var iteration: Int? = null,
    override var iterationsUnsure: Boolean = false
): MediaConsumeEvent {
    override val mediaEntityType: MediaEntityType = MediaEntityType.SONG

    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.MusicNote

    override fun generateMediaRangeMetadata(
        strings: MediaWatchExtensionStrings,
        logStrings: LogFileConverterStrings
    ): String? = null

    override fun copy(
        content: UserContent?,
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, LogEntity.Property<*, *>>?
    ): LogEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure
        )
}
