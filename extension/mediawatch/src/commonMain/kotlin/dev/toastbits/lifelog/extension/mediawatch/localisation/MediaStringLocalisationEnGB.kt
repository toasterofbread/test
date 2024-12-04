package dev.toastbits.lifelog.extension.mediawatch.localisation

import dev.toastbits.lifelog.core.specification.model.string.Locale
import dev.toastbits.lifelog.core.specification.model.string.StringLocalisation
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

internal object MediaStringLocalisationEnGB: StringLocalisation<MediaWatchExtension, MediaStringId>(Locale("en", "GB")) {
    override suspend fun getStringImpl(id: MediaStringId, extension: MediaWatchExtension): String =
        when (id) {
            MediaStringId.MediaExtension.NAME -> "Name"
            MediaStringId.MediaReferenceType.NAME -> "Name"
            MediaStringId.Property.MediaConsumeEvent.MEDIA_REFERENCE -> "Media reference"
            MediaStringId.Property.MediaEntity.ITERATION -> "Iteration"
            MediaStringId.Property.MediaEntityMovieOrShow.RUNTIME -> "Runtime"
            MediaStringId.Property.MediaEntityMovieOrShow.PART_COUNT -> "Part count"
            MediaStringId.MediaConsumeEventType -> "Media consume event"

            is MediaStringId.MediaEntityType ->
                when (id.type) {
                    MediaEntityType.MOVIE_OR_SHOW -> "Movie/show"
                    MediaEntityType.BOOK -> "Book"
                    MediaEntityType.GAME -> "Game"
                    MediaEntityType.SONG -> "Song"
                }

            is MediaStringId.MediaEntityTypeVerb ->
                extension.strings.getMediaEntityTypeConsumeEventPrefixes(id.type).first().trim()
        }
}
