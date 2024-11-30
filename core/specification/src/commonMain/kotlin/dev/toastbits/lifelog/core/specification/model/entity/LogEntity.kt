package dev.toastbits.lifelog.core.specification.model.entity

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.util.LogStringId
import dev.toastbits.lifelog.core.specification.util.StringId

// An entity is anything that can be referenced in user content
interface LogEntity {
    var inlineComment: UserContent?
    var aboveComment: UserContent?

    fun copy(
        inlineComment: UserContent?,
        aboveComment: UserContent?,
        properties: Map<StringId, Property<*, *>>
    ): LogEntity

    fun getCompanion(): LogEntityCompanion<*> = Companion

    data class Property<T: LogEntity, V>(
        val name: StringId,
        val accessor: T.() -> V
    )

    companion object: LogEntityCompanion<LogEntity>(null) {
        override fun getAllProperties(): List<Property<*, *>> =
            listOf(
                LogStringId.Property.LogEntity.COMMENT.property { inlineComment }
            )
    }
}

abstract class LogEntityCompanion<T: LogEntity>(vararg val parents: LogEntityCompanion<*>?) {
    abstract fun getAllProperties(): List<LogEntity.Property<*, *>>

    fun <V> StringId.property(accessor: T.() -> V) =
        LogEntity.Property(this, accessor)
}
