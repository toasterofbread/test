package dev.toastbits.lifelog.core.specification.localisation

import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.string.StringId

internal sealed interface LogStringId: StringId {
    override val extensionId: ExtensionId?
        get() = null

    sealed interface Property: LogStringId {
        enum class LogEntity: Property {
            INLINE_COMMENT,
            ABOVE_COMMENT
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
