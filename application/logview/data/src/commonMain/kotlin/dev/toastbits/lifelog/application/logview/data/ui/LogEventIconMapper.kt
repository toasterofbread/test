package dev.toastbits.lifelog.application.logview.data.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent

fun LogEvent.Icon.toImageVector(): ImageVector =
    when (this) {
        LogEvent.Icon.MusicNote -> Icons.Default.MusicNote
        LogEvent.Icon.Movie -> Icons.Default.Movie
        LogEvent.Icon.Comment -> Icons.Default.ChatBubbleOutline
        LogEvent.Icon.MenuBook -> Icons.AutoMirrored.Filled.MenuBook
        LogEvent.Icon.Gamepad -> Icons.Default.Gamepad
    }
