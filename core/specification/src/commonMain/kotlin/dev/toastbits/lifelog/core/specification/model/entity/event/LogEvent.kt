package dev.toastbits.lifelog.core.specification.model.entity.event

import dev.toastbits.lifelog.core.specification.localisation.LogStringId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity.Property
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.core.specification.model.string.StringId

interface LogEvent: LogEntity {
    val typeName: StringId
    val typeVerb: StringId

    var content: UserContent?

    fun getAllUserContent(): List<UserContent> =
        listOfNotNull(content, aboveComment, inlineComment)

    fun getIcon(): Icon

    suspend fun getPreview(locale: String): LogDisplayText

    suspend fun getTitle(locale: String): LogDisplayText? =
        getPreview(locale)

    fun copy(
        content: UserContent?,
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, Property<*, *>>?
    ): LogEvent

    override fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, Property<*, *>>
    ): LogEntity =
        copy(content, inlineComment, aboveComment, properties)

    override fun getCompanion(): LogEntityCompanion<*> = Companion

    enum class Icon {
        MusicNote,
        Movie,
        Comment,
        MenuBook,
        Gamepad
    }

    companion object: LogEntityCompanion<LogEvent>(LogEntity) {
        override fun getAllProperties(): List<Property<*, *>> =
            listOf(
                LogStringId.Property.LogEvent.CONTENT.property { content }
            )
    }
}
