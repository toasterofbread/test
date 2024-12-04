package dev.toastbits.lifelog.core.specification.model.entity.date

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.localisation.LogStringId
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import kotlinx.datetime.LocalDate

interface LogDate: LogEntity {
    override val extensionId: ExtensionId? get() = null

    var date: LocalDate
    var ambiguous: Boolean

    override fun getCompanion(): LogEntityCompanion<*> = Companion

    companion object: LogEntityCompanion<LogDate>(LogEntity) {
        override fun getAllProperties(): List<LogEntity.Property<*, *>> =
            listOf(
                LogStringId.Property.LogDate.DATE.property { date }
            )
    }
}
