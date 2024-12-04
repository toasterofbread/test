package dev.toastbits.lifelog.core.specification.impl.model.entity.date

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.string.StringId
import kotlinx.datetime.LocalDate

data class LogDateImpl(
    override var date: LocalDate,
    override var ambiguous: Boolean,
    override var inlineComment: UserContent? = null,
    override var aboveComment: UserContent? = null
): LogDate {
    override fun equals(other: Any?): Boolean =
        other is LogDate && date == other.date && ambiguous == other.ambiguous

    override fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, LogEntity.Property<*, *>>
    ): LogEntity =
        copy(
            date = date,
            ambiguous = ambiguous,
            inlineComment = inlineComment,
            aboveComment = aboveComment
        )

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + ambiguous.hashCode()
        return result
    }
}
