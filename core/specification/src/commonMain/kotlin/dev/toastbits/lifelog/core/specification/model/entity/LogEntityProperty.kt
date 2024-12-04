package dev.toastbits.lifelog.core.specification.model.entity

import dev.toastbits.lifelog.core.specification.model.string.StringId

interface LogEntityProperty<T> {
    var value: T
    val name: StringId
}

data class LogEntityPropertyData<T>(
    override val name: StringId,
    override var value: T
) : LogEntityProperty<T>

