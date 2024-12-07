package dev.toastbits.lifelog.application.logview.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.generateUserContent
import dev.toastbits.lifelog.core.specification.converter.parseUserContent
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

internal sealed interface LogEventViewScreenState {
    val type: Type

    sealed class Loaded: LogEventViewScreenState {
        data class Preview(val content: UserContent): Loaded() {
            override val type: Type = Type.PREVIEW
        }
        data class Edit(val content: String): Loaded() {
            override val type: Type = Type.EDIT
        }
    }
    data class Loading private constructor(override val type: Type, val state: Deferred<Loaded>): LogEventViewScreenState {
        companion object {
            inline fun <reified T: Loaded> of(state: Deferred<T>): Loading =
                Loading(
                    when (T::class) {
                        Loaded.Preview::class -> Type.PREVIEW
                        Loaded.Edit::class -> Type.EDIT
                        else -> throw NotImplementedError(T::class.toString())
                    },
                    state
                )
        }
    }

    enum class Type {
        PREVIEW, EDIT
    }
}

internal suspend fun LogEventViewScreenState.awaitLoaded(): LogEventViewScreenState.Loaded =
    when (this) {
        is LogEventViewScreenState.Loaded -> this
        is LogEventViewScreenState.Loading -> state.await()
    }

@Composable
internal fun LogEventViewScreenState.rememberLoadedOrNull(): State<LogEventViewScreenState.Loaded?> {
    val loadedState: MutableState<LogEventViewScreenState.Loaded?> = remember(this) { mutableStateOf(this as? LogEventViewScreenState.Loaded) }
    LaunchedEffect(this) {
        if (this@rememberLoadedOrNull is LogEventViewScreenState.Loading) {
            loadedState.value = state.await()
        }
    }
    return loadedState
}

internal fun LogEventViewScreenState.getNext(
    date: LogDate,
    converter: LogFileConverter,
    coroutineScope: CoroutineScope
): LogEventViewScreenState =
    when (type) {
        LogEventViewScreenState.Type.EDIT ->
            LogEventViewScreenState.Loading.of(
                coroutineScope.async(Dispatchers.Default) {
                    val content: String = (awaitLoaded() as LogEventViewScreenState.Loaded.Edit).content
                    LogEventViewScreenState.Loaded.Preview(
                        converter.parseUserContent(content)
                    )
                }
            )
        LogEventViewScreenState.Type.PREVIEW ->
            LogEventViewScreenState.Loading.of(
                coroutineScope.async(Dispatchers.Default) {
                    val content: UserContent = (awaitLoaded() as LogEventViewScreenState.Loaded.Preview).content
                    LogEventViewScreenState.Loaded.Edit(
                        converter.generateUserContent(content, date)
                    )
                }
            )
    }
