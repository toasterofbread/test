package dev.toastbits.lifelog.core.specification.model.entity

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.localisation.LogStringId
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty

// An entity is anything that can be referenced in user content
interface LogEntity {
    val extensionId: ExtensionId?

    val inlineComment: UserContent?
    val aboveComment: UserContent?

    fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?
    ): LogEntity

    fun getCompanion(): LogEntityCompanion<out LogEntity> = Companion

    companion object: LogEntityCompanion<LogEntity>(null) {
        override fun getProperties(): List<LogEntityProperty<LogEntity, *>> =
            listOf(
                LogStringId.Property.LogEntity.INLINE_COMMENT.userContentProperty({ inlineComment }, { copy(inlineComment = it, aboveComment = aboveComment) }),
                LogStringId.Property.LogEntity.ABOVE_COMMENT.userContentProperty({ aboveComment }, { copy(inlineComment = inlineComment, aboveComment = it) })
            )
    }
}
