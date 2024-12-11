package dev.toastbits.lifelog.application.usercontent.util.logdisplaytext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.toastbits.composekit.util.model.Locale
import dev.toastbits.composekit.util.rememberLocalisedValue
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText

@Composable
fun (suspend (Locale) -> LogDisplayText).rememberLocalisedDisplayText(getDefault: () -> String = { "" }): State<LogDisplayText> =
    rememberLocalisedValue(this) { LogDisplayText.OfString(getDefault()) }

@Composable
fun (suspend (Locale) -> LogDisplayText?).rememberLocalisedDisplayTextNullable(getDefault: () -> String? = { null }): State<LogDisplayText?> =
    rememberLocalisedValue(this) { getDefault()?.let { LogDisplayText.OfString(it) } }
