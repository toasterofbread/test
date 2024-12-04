package dev.toastbits.lifelog.application.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.toastbits.composekit.util.locale.rememberLocalisedValue
import dev.toastbits.lifelog.core.specification.extension.SpecificationExtension
import dev.toastbits.lifelog.core.specification.model.string.Locale
import dev.toastbits.lifelog.core.specification.model.string.StringId

@Composable
fun StringId.rememberLocalisedString(
    extension: SpecificationExtension?,
    getDefault: () -> String = { "" }
): State<String> =
    rememberLocalisedValue(
        getValue = {
            getString(Locale.parse(it), extension)
        },
        getDefault = getDefault
    )
