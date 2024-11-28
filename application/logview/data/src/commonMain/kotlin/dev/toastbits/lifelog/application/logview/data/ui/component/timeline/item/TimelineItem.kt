package dev.toastbits.lifelog.application.logview.data.ui.component.timeline.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal sealed interface TimelineItem {
    @Composable
    fun MainContent(modifier: Modifier)

    @Composable
    fun MetadataItems(itemModifier: Modifier)

    @Composable
    fun IconContent(modifier: Modifier)

    val hasWideIcon: Boolean
        get() = false
}

@Composable
internal fun LogDatabase.rememberTimelineItems(
    key1: Any? = Unit,
    key2: Any? = Unit,
    key3: Any? = Unit,
    filterEvents: (suspend (LogEvent, LogEventReference) -> Boolean)? = null
): State<List<TimelineItem>> {
    val itemsState: MutableState<List<TimelineItem>> = remember { mutableStateOf(emptyList()) }

    LaunchedEffect(key1, key2, key3) {
        withContext(Dispatchers.Default) {
            val sortedDays: List<Map.Entry<LogDate, List<LogEvent>>> =
                this@rememberTimelineItems.days.entries.sortedBy { it.key.date }

            itemsState.value = buildList {
                var dateIndex: Int = 0
                for ((date, events) in sortedDays) {
                    if (events.isEmpty()) {
                        continue
                    }

                    val includedEventIndices: List<Int> =
                        events.mapIndexedNotNull { index, event ->
                            if (filterEvents?.invoke(event, LogEventReference(date, index)) != false) index
                            else null
                        }

                    if (includedEventIndices.isNotEmpty()) {
                        add(DateTimelineItem(date, dateIndex++))
                        for (index in includedEventIndices) {
                            add(EventTimelineItem(LogEventReference(date, index), this@rememberTimelineItems))
                        }
                    }
                }
            }
        }
    }

    return itemsState
}
