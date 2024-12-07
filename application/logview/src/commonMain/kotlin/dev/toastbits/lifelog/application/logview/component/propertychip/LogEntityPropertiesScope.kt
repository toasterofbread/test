package dev.toastbits.lifelog.application.logview.component.propertychip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity

interface LogEntityPropertiesScope<T: LogEntity> {
    val propertyCount: Int

    fun shouldPropertyShow(propertyIndex: Int): Boolean

    @Composable
    fun PropertyChip(
        propertyIndex: Int,
        onEdit: ((LogEntityChanges.Change<T, *>) -> Unit)?,
        modifier: Modifier = Modifier
    )
}
