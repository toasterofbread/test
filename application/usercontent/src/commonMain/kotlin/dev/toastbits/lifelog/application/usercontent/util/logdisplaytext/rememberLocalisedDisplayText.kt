package dev.toastbits.lifelog.application.usercontent.util.logdisplaytext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import dev.toastbits.composekit.util.locale.rememberLocalisedValue
import dev.toastbits.lifelog.core.specification.model.entity.LogDisplayText

@Composable
fun (suspend (String) -> LogDisplayText).rememberLocalisedDisplayText(getDefault: () -> String = { "" }): State<LogDisplayText> =
    rememberLocalisedValue(this) { LogDisplayText.OfString(getDefault()) }

@Composable
fun (suspend (String) -> LogDisplayText?).rememberLocalisedDisplayTextNullable(getDefault: () -> String? = { null }): State<LogDisplayText?> =
    rememberLocalisedValue(this) { getDefault()?.let { LogDisplayText.OfString(it) } }
