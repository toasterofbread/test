package dev.toastbits.lifelog.application.logview.component.propertychip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty

interface LogEntityPropertiesScope<T: LogEntity> {
    val properties: List<LogEntityProperty<T, *>>

    @Composable
    fun <V> PropertyChip(
        property: LogEntityProperty<T, V>,
        onEdit: ((LogEntityChanges.Change<T, V>) -> Unit)?,
        modifier: Modifier = Modifier
    )
}
