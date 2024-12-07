package dev.toastbits.lifelog.extension.mediawatch.model.entity

import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty

sealed interface MediaEntity: LogEntity {
    override fun getCompanion(): LogEntityCompanion<*> = Companion

    companion object: LogEntityCompanion<MediaEntity>(LogEntity) {
        override fun getProperties(): List<LogEntityProperty<MediaEntity, *>> = emptyList()
    }
}
