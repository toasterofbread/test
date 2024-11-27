package dev.toastbits.lifelog.application.core.usercontent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.vibrantAccent
import dev.toastbits.composekit.util.indexOfFirstOrNull
import dev.toastbits.composekit.util.thenWith
import dev.toastbits.lifelog.application.core.usercontent.model.ModsState
import dev.toastbits.lifelog.application.core.usercontent.model.getState
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

private val LocalReference: ProvidableCompositionLocal<LogEntityReference?> =
    compositionLocalOf { null }

@Composable
fun UserContentDisplay(content: UserContent, modifier: Modifier = Modifier) {
    SelectionContainer(modifier) {
        FlowRow {
            for (part in content.parts) {
                UserContentPart(part)
            }
        }
    }
}

@Composable
fun UserContentPart(part: UserContent.Part) {
    WithMods(part.mods) {
        when (part) {
            is UserContent.Part.Composite -> {
                for (subpart in part.parts) {
                    UserContentPart(subpart)
                }
            }
            is UserContent.Part.Image -> {
                AsyncImage(model = part.location, contentDescription = null)
            }
            is UserContent.Part.Single -> {
                SinglePart(part)
            }
        }
    }
}

@Composable
private fun SinglePart(part: UserContent.Part.Single) {
    val reference: LogEntityReference? = LocalReference.current
    val textParts: List<String> = part.text.split(' ')

    for ((textIndex, text) in textParts.withIndex()) {
        val leadingNewlines: Int =
            text.indexOfFirstOrNull { it != '\n' } ?: text.length

        val trailingNewlines: Int =
            text.indexOfLast { it != '\n' }
                .takeIf { it != -1 }
                ?.let { lastNewline -> text.length - lastNewline - 1 }
                ?: 0

        for (i in 0 until leadingNewlines) {
            Spacer(Modifier.fillMaxWidth())
        }

        val subparts: List<String> =
            text.substring(leadingNewlines, text.length - trailingNewlines).split('\n')

        for ((subpartIndex, subpart) in subparts.withIndex()) {
            Text(
                if (subpartIndex + 1 == subparts.size && textIndex + 1 != textParts.size) "$subpart "
                else subpart,
                Modifier
                    .thenWith(reference) { ref ->
                        clickable(
                            remember { MutableInteractionSource() },
                            null
                        ) {
                            TODO("Open reference $ref")
                        }
                    }
            )

            if (subpartIndex + 1 != subparts.size) {
                Spacer(Modifier.fillMaxWidth())
            }
        }

        for (i in 0 until trailingNewlines) {
            Spacer(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun WithMods(parts: Collection<UserContent.Mod>, content: @Composable () -> Unit) {
    val state: ModsState = remember(parts.hashCode()) { parts.getState() }
    val textStyle: TextStyle = LocalTextStyle.current

    CompositionLocalProvider(
        LocalTextStyle providesComputed {
            textStyle.copy(
                fontWeight = if (state.bold) FontWeight.Bold else textStyle.fontWeight,
                fontStyle = if (state.italic) FontStyle.Italic else textStyle.fontStyle,
                textDecoration = if (state.strikethrough) TextDecoration.LineThrough else textStyle.textDecoration,
                color = if (state.reference != null) LocalComposeKitTheme.currentValue.vibrantAccent else textStyle.color
            )
        },
        LocalReference provides state.reference
    ) {
        when (state.container) {
            ModsState.Container.CODE_LINE,
            ModsState.Container.CODE_BLOCK -> CodeBlock(content = content)
            null -> content()
        }
    }
}

@Composable
private fun CodeBlock(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val theme: ThemeValues = LocalComposeKitTheme.current

    Box(
        modifier
            .background(theme.card)
            .padding(10.dp)
    ) {
        content()
    }
}
