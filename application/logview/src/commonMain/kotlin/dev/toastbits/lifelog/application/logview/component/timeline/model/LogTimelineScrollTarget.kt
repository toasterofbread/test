package dev.toastbits.lifelog.application.logview.component.timeline.model

import dev.toastbits.lifelog.application.logview.component.timeline.item.DateTimelineItem
import dev.toastbits.lifelog.application.logview.component.timeline.item.TimelineItem
import dev.toastbits.lifelog.application.logview.model.LogEventReference

internal sealed interface LogTimelineScrollTarget {
    fun getTargetScrollIndex(timelineItems: List<TimelineItem>): Int?

    data class LogEvent(val event: LogEventReference): LogTimelineScrollTarget {
        override fun getTargetScrollIndex(timelineItems: List<TimelineItem>): Int? =
            timelineItems.indexOfFirst { (it as? DateTimelineItem)?.date == event.date }
                .takeIf { it != -1 }
                ?.plus(event.logIndex)
    }
    data class DateIndex(val dateIndex: Int, val offset: Int = 0): LogTimelineScrollTarget {
        override fun getTargetScrollIndex(timelineItems: List<TimelineItem>): Int? {
            val dateIndex: Int =
                if (dateIndex == Int.MAX_VALUE)
                    (timelineItems.lastOrNull { it is DateTimelineItem } as DateTimelineItem?)?.index
                        ?: return null
                else if (dateIndex == Int.MIN_VALUE) 0
                else dateIndex

            return (
                timelineItems
                    .indexOfFirst { item ->
                        (item as? DateTimelineItem)?.index == dateIndex
                    }
                    .takeIf { it != -1 }
                    ?.plus(offset)
            )
        }
    }
}