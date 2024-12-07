package dev.toastbits.lifelog.core.specification.model.entity.property

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceType
import dev.toastbits.lifelog.core.specification.model.string.StringId
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

sealed class LogEntityProperty<T: LogEntity, V> {
    abstract val name: StringId

    protected abstract val getter: T.() -> V
    protected abstract val setter: T.(V) -> T

    open fun shouldShow(entity: T): Boolean = true
    open fun isValueAllowed(entity: T, value: V): Boolean = true
    open fun areValuesEquivalent(a: V, b: V): Boolean = a == b

    fun getValue(entity: T): V = getter(entity)
    fun setValue(entity: T, value: V): T {
        require(isValueAllowed(entity, value))
        return setter(entity, value)
    }

    override fun toString(): String =
        "${this::class.simpleName}(name=$name)"
}

class IntLogEntityProperty<T: LogEntity>(
    override val name: StringId,
    override val getter: T.() -> Int?,
    override val setter: T.(Int?) -> T,
    val allowedRange: IntRange = Int.MIN_VALUE .. Int.MAX_VALUE
): LogEntityProperty<T, Int?>() {
    override fun shouldShow(entity: T): Boolean =
        getValue(entity) != null

    override fun isValueAllowed(entity: T, value: Int?): Boolean =
        value == null || value in allowedRange
}

class DurationLogEntityProperty<T: LogEntity>(
    override val name: StringId,
    override val getter: T.() -> Duration?,
    override val setter: T.(Duration?) -> T
): LogEntityProperty<T, Duration?>(){
    override fun shouldShow(entity: T): Boolean =
        getValue(entity) != null
}

class UserContentLogEntityProperty<T: LogEntity>(
    override val name: StringId,
    override val getter: T.() -> UserContent?,
    override val setter: T.(UserContent?) -> T
): LogEntityProperty<T, UserContent?>() {
    override fun shouldShow(entity: T): Boolean =
        getValue(entity) != null

    override fun areValuesEquivalent(a: UserContent?, b: UserContent?): Boolean =
        a == b || ((a != null && b != null) && (a.isEmpty() && b.isEmpty()) || a?.normalised() == b?.normalised())
}

class LocalDateLogEntityProperty<T: LogEntity>(
    override val name: StringId,
    override val getter: T.() -> LocalDate,
    override val setter: T.(LocalDate) -> T
): LogEntityProperty<T, LocalDate>()

class EntityReferenceLogEntityProperty<T: LogEntity>(
    override val name: StringId,
    override val getter: T.() -> LogEntityReference?,
    override val setter: T.(LogEntityReference?) -> T,
    private val allowedTypes: List<LogEntityReferenceType>?
): LogEntityProperty<T, LogEntityReference?>() {
    override fun shouldShow(entity: T): Boolean =
        getValue(entity) != null

    override fun isValueAllowed(entity: T, value: LogEntityReference?): Boolean =
        TODO(allowedTypes.toString())
}
