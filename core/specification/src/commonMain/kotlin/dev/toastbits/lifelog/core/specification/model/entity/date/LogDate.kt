package dev.toastbits.lifelog.core.specification.model.entity.date

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.localisation.LogStringId
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import kotlinx.datetime.LocalDate

interface LogDate: LogEntity {
    override val extensionId: ExtensionId? get() = null

    val date: LocalDate
    val ambiguous: Boolean

    fun copy(
        date: LocalDate,
        ambiguous: Boolean
    ): LogDate

    override fun getCompanion(): LogEntityCompanion<*> = Companion

    companion object: LogEntityCompanion<LogDate>(LogEntity) {
        override fun getProperties(): List<LogEntityProperty<LogDate, *>> =
            listOf(
                LogStringId.Property.LogDate.DATE.dateProperty({ date }, { copy(date = date, ambiguous = ambiguous)})
            )
    }
}
