package dev.toastbits.lifelog.application.logview.data.ui.model

import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.converter.parseUserContent
import dev.toastbits.lifelog.core.specification.model.UserContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal sealed interface LogEventViewScreenState {
    val type: Type

    data class Previewing(val content: UserContent): LogEventViewScreenState {
        override val type: Type = Type.PREVIEW
    }
    data class Editing(val content: String): LogEventViewScreenState {
        override val type: Type = Type.EDIT
    }

    enum class Type {
        PREVIEW, EDIT
    }
}

internal fun LogEventViewScreenState.getNextType(): LogEventViewScreenState.Type =
    when (this) {
        is LogEventViewScreenState.Editing -> LogEventViewScreenState.Type.PREVIEW
        is LogEventViewScreenState.Previewing -> LogEventViewScreenState.Type.EDIT
    }

internal suspend fun LogEventViewScreenState.getNext(
    eventReference: LogEventReference,
    converter: LogFileConverter
): LogEventViewScreenState = withContext(Dispatchers.Default) {
    when (this@getNext) {
        is LogEventViewScreenState.Editing ->
            LogEventViewScreenState.Previewing(
                converter.parseUserContent(content)
            )
        is LogEventViewScreenState.Previewing ->
            LogEventViewScreenState.Editing(
                converter.generateUserContent(content, eventReference.date)
            )
    }
}
