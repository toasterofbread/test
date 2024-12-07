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
    val properties: List<LogEntityProperty<T, *>> =
        remember(this) {
            getCompanion().getAllProperties() as List<LogEntityProperty<T, *>>
        }

    val scope: LogEntityPropertiesScope<T> =
        remember(properties) {
            object : LogEntityPropertiesScope<T> {
                override val propertyCount: Int
                    get() = properties.size

                override fun shouldPropertyShow(propertyIndex: Int): Boolean =
                    properties[propertyIndex].shouldShow(this@withProperties)

                @Composable
                override fun PropertyChip(
                    propertyIndex: Int,
                    onEdit: ((LogEntityChanges.Change<T, *>) -> Unit)?,
                    modifier: Modifier
                ) {
                    val property: LogEntityProperty<T, *> = properties[propertyIndex]
                    PropertyChip(property, onEdit, modifier)
                }

                @Composable
                private fun <V> PropertyChip(
                    property: LogEntityProperty<T, V>,
                    onEdit: ((LogEntityChanges.Change<T, *>) -> Unit)?,
                    modifier: Modifier = Modifier
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
