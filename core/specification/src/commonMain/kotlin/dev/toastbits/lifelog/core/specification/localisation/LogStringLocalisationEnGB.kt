package dev.toastbits.lifelog.core.specification.localisation

import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.string.Locale
import dev.toastbits.lifelog.core.specification.model.string.StringLocalisation

internal object LogStringLocalisationEnGB: StringLocalisation<SpecificationExtension, LogStringId>(Locale("en", "GB")) {
    override suspend fun getStringImpl(id: LogStringId, extension: SpecificationExtension): String =
        when (id) {
            LogStringId.Property.LogDate.DATE -> "Date"
            LogStringId.Property.LogEntity.INLINE_COMMENT -> "Inline comment"
            LogStringId.Property.LogEntity.ABOVE_COMMENT -> "Above comment"
            LogStringId.Property.LogEvent.CONTENT -> "Content"
            LogStringId.LogCommentEvent -> "Comment"
            LogStringId.LogCommentEventVerb -> "Commented"
        }
}
