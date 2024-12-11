package dev.toastbits.lifelog.core.specification.model.entity.event

import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.lifelog.core.specification.localisation.LogStringId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import dev.toastbits.lifelog.core.specification.model.string.StringId

interface LogEvent: LogEntity {
    val typeName: StringId
    val typeVerb: StringId

    val content: UserContent?

    fun getAllUserContent(): List<UserContent> =
        listOfNotNull(content, aboveComment, inlineComment)

    fun getIcon(): Icon

    suspend fun getPreview(locale: Locale): LogDisplayText

    suspend fun getTitle(locale: Locale): LogDisplayText? =
        getPreview(locale)

    override fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?
    ): LogEvent

    fun copy(
        content: UserContent?
    ): LogEvent

    enum class Icon {
        MusicNote,
        Movie,
        Comment,
        MenuBook,
        Gamepad
    }

    override fun getCompanion(): LogEntityCompanion<out LogEvent> = Companion

    companion object: LogEntityCompanion<LogEvent>(LogEntity) {
        val PROPERTY_CONTENT: LogEntityProperty<LogEvent, UserContent?> =
            LogStringId.Property.LogEvent.CONTENT.userContentProperty({ content }, { copy(content = it) })

        override fun getProperties(): List<LogEntityProperty<LogEvent, *>> =
            listOf(
                PROPERTY_CONTENT
            )
    }
}
