package dev.toastbits.lifelog.core.specification.model.entity

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.property.DurationLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.EntityReferenceLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.IntLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.LocalDateLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.UserContentLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import dev.toastbits.lifelog.core.specification.model.string.StringId
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

abstract class LogEntityCompanion<T: LogEntity>(private vararg val parents: LogEntityCompanion<*>?) {
    protected abstract fun getProperties(): List<LogEntityProperty<T, *>>

    @Suppress("UNCHECKED_CAST")
    private fun getChain(): List<LogEntityCompanion<T>> =
        listOf(this) + (parents.flatMap { it?.getChain().orEmpty() } as List<LogEntityCompanion<T>>)

    fun getAllProperties(): List<LogEntityProperty<T, *>> =
        getChain().flatMap { it.getProperties() }

    protected fun StringId.intProperty(
        getter: T.() -> Int?,
        setter: T.(Int?) -> T,
        allowedRange: IntRange = Int.MIN_VALUE .. Int.MAX_VALUE
    ): LogEntityProperty<T, Int?> =
        IntLogEntityProperty(this, getter, setter, allowedRange)

    protected fun StringId.durationProperty(
        getter: T.() -> Duration?,
        setter: T.(Duration?) -> T
    ): LogEntityProperty<T, Duration?> =
        DurationLogEntityProperty(this, getter, setter)

    protected fun StringId.userContentProperty(
        getter: T.() -> UserContent?,
        setter: T.(UserContent?) -> T
    ): LogEntityProperty<T, UserContent?> =
        UserContentLogEntityProperty(this, getter, setter)

    protected fun StringId.dateProperty(
        getter: T.() -> LocalDate,
        setter: T.(LocalDate) -> T
    ): LogEntityProperty<T, LocalDate> =
        LocalDateLogEntityProperty(this, getter, setter)

    protected fun StringId.entityReferenceProperty(
        getter: T.() -> LogEntityReference?,
        setter: T.(LogEntityReference?) -> T,
        allowedTypes: List<LogEntityReferenceType>? = null
    ): LogEntityProperty<T, LogEntityReference?> =
        EntityReferenceLogEntityProperty(this, getter, setter, allowedTypes)
}
