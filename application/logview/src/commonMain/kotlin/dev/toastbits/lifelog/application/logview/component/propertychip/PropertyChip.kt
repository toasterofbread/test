package dev.toastbits.lifelog.application.logview.component.propertychip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.onAccent
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.core.ui.util.rememberLocalisedString
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.property.DurationLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.EntityReferenceLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.IntLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.LocalDateLogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import dev.toastbits.lifelog.core.specification.model.entity.property.UserContentLogEntityProperty

@Composable
fun <T: LogEntity, V> LogEntityProperty<T, V>.PropertyChip(
    entity: T,
    configuration: LogDatabaseConfiguration,
    onEdit: ((V) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val theme: ThemeValues = LocalComposeKitTheme.current
    val propertyName: String by name.rememberLocalisedString(configuration)

    val propertyOnEdit: ((V) -> Unit)? =
        remember(this, onEdit) {
            if (onEdit == null) null
            else { newValue ->
                if (isValueAllowed(entity, newValue)) {
                    onEdit(newValue)
                }
            }
        }

    Box(
        modifier
            .background(
                theme.accent,
                MaterialTheme.shapes.small
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelLarge,
            LocalContentColor provides theme.onAccent
        ) {
            when (this@PropertyChip) {
                is IntLogEntityProperty ->
                    IntLogEntityPropertyChip(
                        propertyName,
                        getValue(entity),
                        entity,
                        this@PropertyChip,
                        propertyOnEdit as ((Int?) -> Unit)?
                    )
                is DurationLogEntityProperty -> { Text(this@PropertyChip::class.simpleName.toString()) }
                is EntityReferenceLogEntityProperty -> { Text(this@PropertyChip::class.simpleName.toString()) }
                is LocalDateLogEntityProperty -> { Text(this@PropertyChip::class.simpleName.toString()) }
                is UserContentLogEntityProperty -> { Text(this@PropertyChip::class.simpleName.toString()) }
            }
        }
    }
}
