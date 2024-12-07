package dev.toastbits.lifelog.application.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.toastbits.composekit.util.locale.rememberLocalisedValue
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.model.string.Locale
import dev.toastbits.lifelog.core.specification.model.string.StringId

@Composable
fun StringId.rememberLocalisedString(
    configuration: LogDatabaseConfiguration,
    getDefault: () -> String = { "" }
): State<String> =
    rememberLocalisedValue(
        getValue = { locale ->
            getString(
                Locale.parse(locale),
                extensionId?.let { configuration.extensionRegistry.findRegisteredExtension(it) }
            )
        },
        getDefault = getDefault
    )
