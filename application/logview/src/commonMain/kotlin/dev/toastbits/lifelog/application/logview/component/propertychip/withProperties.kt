package dev.toastbits.lifelog.application.logview.component.propertychip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty

@Composable
fun <T: LogEntity> T.withProperties(
    configuration: LogDatabaseConfiguration,
    content: @Composable LogEntityPropertiesScope<T>.() -> Unit
) {
    val scope: LogEntityPropertiesScope<T> =
        remember(this) {
            object : LogEntityPropertiesScope<T> {
                @Suppress("UNCHECKED_CAST")
                override val properties: List<LogEntityProperty<T, *>> =
                    getCompanion().getAllProperties() as List<LogEntityProperty<T, *>>

                @Composable
                override fun <V> PropertyChip(
                    property: LogEntityProperty<T, V>,
                    onEdit: ((LogEntityChanges.Change<T, V>) -> Unit)?,
                    modifier: Modifier
                ) {
                    property.PropertyChip(
                        entity = this@withProperties,
                        configuration = configuration,
                        onEdit = onEdit?.let { lambda ->
                            { newValue ->
                                lambda(LogEntityChanges.Change(property, newValue))
                            }
                        },
                        modifier = modifier
                    )
                }
            }
        }

    content(scope)
}
