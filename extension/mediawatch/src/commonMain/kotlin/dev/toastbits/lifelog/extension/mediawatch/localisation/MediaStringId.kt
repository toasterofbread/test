package dev.toastbits.lifelog.extension.mediawatch.localisation

import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.string.Locale
import dev.toastbits.lifelog.core.specification.model.string.StringId

internal sealed interface MediaStringId: StringId {
    sealed interface Property: MediaStringId {
        enum class MediaEntity: Property {
            ITERATION
        }

        enum class MediaEntityMovieOrShow: Property {
            RUNTIME,
            PART_COUNT
        }

        enum class MediaConsumeEvent: Property {
            MEDIA_REFERENCE
        }
    }

    enum class MediaExtension: MediaStringId {
        NAME
    }

    enum class MediaReferenceType: MediaStringId {
        NAME
    }

    data class MediaEntityType(val type: dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType): MediaStringId
    data class MediaEntityTypeVerb(val type: dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType): MediaStringId

    data object MediaConsumeEventType: MediaStringId

    override suspend fun getString(locale: Locale, extension: SpecificationExtension?): String =
        MediaStringLocalisations.getBestLocalisation(locale).getString(this, extension)
}
