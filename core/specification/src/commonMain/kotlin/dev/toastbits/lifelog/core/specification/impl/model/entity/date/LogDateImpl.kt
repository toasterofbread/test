package dev.toastbits.lifelog.core.specification.impl.model.entity.date

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class LogDateImpl(
    override val date: LocalDate,
    override val ambiguous: Boolean,
    override val inlineComment: UserContent? = null,
    override val aboveComment: UserContent? = null
): LogDate {
    override fun equals(other: Any?): Boolean =
        other is LogDate && date == other.date && ambiguous == other.ambiguous

    override fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?
    ): LogEntity =
        copy(
            date = date,
            ambiguous = ambiguous,
            inlineComment = inlineComment,
            aboveComment = aboveComment
        )

    override fun copy(
        date: LocalDate,
        ambiguous: Boolean
    ): LogDate =
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

    companion object {
        fun now(): LogDateImpl =
            LogDateImpl(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                false
            )
    }
}
