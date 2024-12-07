package dev.toastbits.lifelog.extension.mediawatch.model.entity.event

import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtensionStrings
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

data class SongMediaConsumeEvent(
    override val mediaReference: MediaReference,
    override val inlineComment: UserContent? = null,
    override val aboveComment: UserContent? = null,
    override val content: UserContent? = null,
    override val iteration: Int? = null,
    override val iterationsUnsure: Boolean = false
): MediaConsumeEvent {
    override val extensionId: ExtensionId = MediaWatchExtension.ID
    override val mediaEntityType: MediaEntityType = MediaEntityType.SONG

    override fun getIcon(): LogEvent.Icon = LogEvent.Icon.MusicNote

    override fun generateMediaRangeMetadata(
        strings: MediaWatchExtensionStrings,
        logStrings: LogFileConverterStrings
    ): String? = null

    override fun copy(inlineComment: UserContent?, aboveComment: UserContent?): MediaConsumeEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure
        )

    override fun copy(content: UserContent?): MediaConsumeEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure
        )

    override fun copy(
        mediaReference: MediaReference,
        iteration: Int?,
        iterationsUnsure: Boolean
    ): MediaConsumeEvent =
        copy(
            mediaReference = mediaReference,
            inlineComment = inlineComment,
            aboveComment = aboveComment,
            content = content,
            iteration = iteration,
            iterationsUnsure = iterationsUnsure
        )
}
