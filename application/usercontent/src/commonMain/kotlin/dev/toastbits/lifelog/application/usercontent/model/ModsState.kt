package dev.toastbits.lifelog.application.usercontent.model

import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

internal data class ModsState(
    var bold: Boolean = false,
    var italic: Boolean = false,
    var strikethrough: Boolean = false,
    var container: Container? = null,
    var reference: LogEntityReference? = null
) {
    fun updateContainer(newContainer: Container) {
        if (container == null || container!!.ordinal < newContainer.ordinal) {
            container = newContainer
        }
    }

    enum class Container {
        CODE_LINE,
        CODE_BLOCK
    }
}

internal fun Collection<UserContent.Mod>.getState(): ModsState {
    val state: ModsState = ModsState()
    for (mod in this) {
        when (mod) {
            UserContent.Mod.Bold -> state.bold = true
            UserContent.Mod.Italic -> state.italic = true
            UserContent.Mod.Strikethrough -> state.strikethrough = true
            UserContent.Mod.Code -> state.updateContainer(ModsState.Container.CODE_LINE)
            UserContent.Mod.CodeBlock -> state.updateContainer(ModsState.Container.CODE_BLOCK)
            is UserContent.Mod.Reference -> state.reference = mod.reference
        }
    }
    return state
}
