package dev.toastbits.lifelog.application.logview.data.ui.component.eventview

import androidx.compose.foundation.text.input.TextFieldState
import dev.toastbits.lifelog.application.logview.data.ui.screen.LogEventReference
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.model.UserContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal sealed interface LogEventViewScreenState {
    val type: Type

    data class Previewing(val content: UserContent): LogEventViewScreenState {
        override val type: Type = Type.PREVIEW
    }
    data class Editing(val initialContent: String): LogEventViewScreenState {
        override val type: Type = Type.EDIT
        val textFieldState: TextFieldState = TextFieldState(initialContent)
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
                converter.userContentParser.parseUserContent(
                    textFieldState.text.toString(),
                    converter.referenceParser,
                    onAlert = { alert, line ->
                        println("ALERT 2 $alert $line")
                    }
                )
            )
        is LogEventViewScreenState.Previewing ->
            LogEventViewScreenState.Editing(
                converter.userContentGenerator.generateUserContent(
                    content,
                    converter.referenceGeneratorProvider(eventReference.date.date),
                    onAlert = { alert, line ->
                        println("ALERT $alert $line")
                    }
                )
            )
    }
}
