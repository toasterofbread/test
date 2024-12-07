package dev.toastbits.lifelog.application.logview.model

import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty

class LogEntityChanges<T: LogEntity> private constructor(
    val changesList: List<Change<T, *>> = emptyList()
) {
    fun hasChanges(entity: T): Boolean =
        changesList.any { it.changesEntity(entity) }

    fun applyTo(entity: T): T {
        var currentEntity: T = entity
        for (change in changesList) {
            currentEntity = change.applyTo(currentEntity)
        }
        return currentEntity
    }

    fun <V> copyWithProperty(property: LogEntityProperty<in T, V>, newValue: V): LogEntityChanges<T> =
        copyWithChange(Change(property as LogEntityProperty<T, V>, newValue))

    fun <V> copyWithChange(change: Change<T, V>): LogEntityChanges<T> =
        LogEntityChanges(
            changesList = changesList.filterNot { it.property == change.property } + change
        )

    @Suppress("UNCHECKED_CAST")
    fun <V> firstWithPropertyOrNull(property: LogEntityProperty<in T, V>): Change<T, V>? =
        changesList.firstOrNull { it.property == property } as Change<T, V>?

    data class Change<T: LogEntity, V>(
        val property: LogEntityProperty<T, V>,
        val newValue: V
    ) {
        fun changesEntity(entity: T): Boolean =
            !property.areValuesEquivalent(newValue, property.getValue(entity))

        fun applyTo(entity: T): T =
            property.setValue(entity, newValue)
    }

    override fun toString(): String =
        "LogEntityChanges($changesList)"

    companion object {
        fun <T: LogEntity> createEmpty(): LogEntityChanges<T> = LogEntityChanges()
    }
}