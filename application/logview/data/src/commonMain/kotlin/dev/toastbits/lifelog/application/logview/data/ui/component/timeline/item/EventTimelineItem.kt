package dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.lifelog.application.logview.data.ui.screen.LogEventReference
import dev.toastbits.lifelog.application.logview.data.ui.screen.get
import dev.toastbits.lifelog.application.logview.data.ui.toImageVector
import dev.toastbits.lifelog.core.specification.database.LogDatabase

data class EventTimelineItem(
    val event: LogEventReference,
    val logDatabase: LogDatabase
): TimelineItem {
    @Composable
    override fun MainContent(modifier: Modifier) {
        Text(logDatabase[event].toString().take(100), modifier)
    }

    @Composable
    override fun MetadataItems(itemModifier: Modifier) {
        Text("What", itemModifier)
        Text("The", itemModifier)
        Text("Heck", itemModifier)
        Text("Is", itemModifier)
        Text("A", itemModifier)
        Text("Metadata", itemModifier)
        Text("Item", itemModifier)
    }

    @Composable
    override fun IconContent(modifier: Modifier) {
        val theme: ThemeValues = LocalComposeKitTheme.current
        val shape: Shape = CircleShape

        Box(
            modifier
                .background(theme.background, shape)
                .border(2.dp, theme.accent, shape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(logDatabase[event].getIcon().toImageVector(), null)
        }
    }
}
