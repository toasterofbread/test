package dev.toastbits.lifelog.core.specification.localisation

import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.string.Locale
import dev.toastbits.lifelog.core.specification.model.string.StringId

internal sealed interface LogStringId: StringId {
    sealed interface Property: LogStringId {
        enum class LogEntity: Property {
            COMMENT
        }
        enum class LogEvent: Property {
            CONTENT
        }
        enum class LogDate: Property {
            DATE
        }
    }

    data object LogCommentEvent: LogStringId
    data object LogCommentEventVerb: LogStringId

    override suspend fun getString(
        locale: Locale,
        extension: SpecificationExtension?
    ): String =
        LogStringLocalisations.getBestLocalisation(locale).getString(this, extension)
}
