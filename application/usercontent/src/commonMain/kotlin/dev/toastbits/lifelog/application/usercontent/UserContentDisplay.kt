package dev.toastbits.lifelog.application.usercontent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.LocalContext
import dev.toastbits.composekit.context.PlatformContext
import dev.toastbits.composekit.theme.core.ThemeValues
import dev.toastbits.composekit.theme.core.ui.LocalComposeKitTheme
import dev.toastbits.composekit.theme.core.vibrantAccent
import dev.toastbits.composekit.util.indexOfFirstOrNull
import dev.toastbits.composekit.util.thenWith
import dev.toastbits.lifelog.application.usercontent.generated.resources.Res
import dev.toastbits.lifelog.application.usercontent.generated.resources.user_content_display_empty_indicator
import dev.toastbits.lifelog.application.usercontent.model.ModsState
import dev.toastbits.lifelog.application.usercontent.model.getState
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

private val LocalReference: ProvidableCompositionLocal<LogEntityReference?> =
    compositionLocalOf { null }

data class UserContentDisplayResult(
    val singleLine: Boolean
)

@Composable
fun UserContentDisplay(
    content: UserContent,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    onDisplayResult: ((UserContentDisplayResult) -> Unit)? = null,
    container: @Composable (Modifier, @Composable () -> Unit) -> Unit = { modifier, content ->
        UserContentDisplayContainer(modifier, content)
    }
) {
    var containerPosition: Float by remember { mutableStateOf(0f) }
    val partPositions: MutableMap<Int, Float> = remember { mutableStateMapOf() }
    val singleLineTextParts: MutableMap<Int, Boolean> = remember { mutableStateMapOf() }

    LaunchedEffect(onDisplayResult) {
        if (onDisplayResult == null) {
            return@LaunchedEffect
        }

        snapshotFlow {
            UserContentDisplayResult(
                singleLine =
                    singleLineTextParts.values.all { it }
                        && partPositions.values.all { it == containerPosition }
            )
        }.collectLatest(onDisplayResult)
    }

    SelectionContainer(modifier) {
        val isBlank: Boolean =
            remember(content) { content.isBlank() }

        if (isBlank) {
            BlankUserContentIndicator(Modifier.fillMaxWidth())
            return@SelectionContainer
        }

        container(
            Modifier.onGloballyPositioned {
                containerPosition = it.positionInRoot().y
            }
        ) {
            for ((index, part) in content.parts.withIndex()) {
                val scope: UserContentDisplayScope =
                    remember(index, onDisplayResult) {
                        if (onDisplayResult == null) {
                            return@remember object : UserContentDisplayScope {}
                        }

                        object : UserContentDisplayScope {
                            override val itemModifier: Modifier =
                                Modifier.onGloballyPositioned {
                                    partPositions[index] = it.positionInRoot().y
                                }

                            override val onTextLayout: (TextLayoutResult) -> Unit = { textLayoutResult ->
                                singleLineTextParts[index] = textLayoutResult.lineCount <= 1
                            }
                        }
                    }

                scope.UserContentPart(
                    part,
                    textStyle
                )
            }
        }
    }
}

@Composable
private fun UserContentDisplayContainer(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val density: Density = LocalDensity.current
    var contentHeight: Dp by remember { mutableStateOf(0.dp) }

    FlowRow(
        modifier
            // Workaround for FlowRow bug(?) in which its reported height is smaller than the actual content
            .height(contentHeight)
            .wrapContentHeight(Alignment.Top, unbounded = true)
            .onSizeChanged {
                with(density) {
                    contentHeight = it.height.toDp()
                }
            }
    ) {
        content()
    }
}

private interface UserContentDisplayScope {
    val itemModifier: Modifier get() = Modifier
    val onTextLayout: ((textLayoutResult: TextLayoutResult) -> Unit)? get() = null
}

@Composable
private fun UserContentDisplayScope.UserContentPart(
    part: UserContent.Part,
    textStyle: TextStyle
) {
    WithMods(part.mods, textStyle) {
        when (part) {
            is UserContent.Part.Composite -> {
                for (subpart in part.parts) {
                    UserContentPart(subpart, LocalTextStyle.current)
                }
            }
            is UserContent.Part.Image -> {
                Text(
                    "Image<${part.location}> // TODO",
                    itemModifier,
                    onTextLayout = onTextLayout
                )
//                val painter: Painter = rememberImagePainter(part.location)
//                Image(painter, contentDescription = null)
            }
            is UserContent.Part.Single -> {
                SinglePart(part)
            }
        }
    }
}

@Composable
private fun BlankUserContentIndicator(modifier: Modifier = Modifier) {
    Text(
        stringResource(Res.string.user_content_display_empty_indicator),
        modifier,
        color = LocalComposeKitTheme.current.vibrantAccent,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun UserContentDisplayScope.SinglePart(part: UserContent.Part.Single) {
    val reference: LogEntityReference? = LocalReference.current
    val context: PlatformContext = LocalContext.current
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
            Spacer(itemModifier.fillMaxWidth())
        }

        val subparts: List<String> =
            text.substring(leadingNewlines, text.length - trailingNewlines).split('\n')

        for ((subpartIndex, subpart) in subparts.withIndex()) {
            Text(
                if (subpartIndex + 1 == subparts.size && textIndex + 1 != textParts.size) "$subpart "
                else subpart,
                itemModifier
                    .thenWith(reference) { ref ->
                        clickable(
                            remember { MutableInteractionSource() },
                            null
                        ) {
                            when (ref) {
                                is LogEntityReference.InLog -> TODO(ref.toString())
                                is LogEntityReference.InMetadata -> TODO(ref.toString())
                                is LogEntityReference.URL -> {
                                    if (context.canOpenUrl()) {
                                        context.openUrl(ref.url)
                                    } else if (context.canShare()) {
                                        context.shareText(ref.url)
                                    } else if (context.canCopyText()) {
                                        context.copyText(ref.url)
                                    }
                                }
                            }
                        }
                            .pointerHoverIcon(PointerIcon.Hand, true)
                    },
                onTextLayout = onTextLayout
            )

            if (subpartIndex + 1 != subparts.size) {
                Spacer(itemModifier.fillMaxWidth())
            }
        }

        for (i in 0 until trailingNewlines) {
            Spacer(itemModifier.fillMaxWidth())
        }
    }
}

@Composable
private fun WithMods(
    parts: Collection<UserContent.Mod>,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val state: ModsState = remember(parts.hashCode()) { parts.getState() }
    val typography: Typography = MaterialTheme.typography

    CompositionLocalProvider(
        LocalTextStyle providesComputed {
            textStyle.copy(
                fontWeight = if (state.bold) FontWeight.Bold else textStyle.fontWeight,
                fontStyle = if (state.italic) FontStyle.Italic else textStyle.fontStyle,
                textDecoration = if (state.strikethrough) TextDecoration.LineThrough else textStyle.textDecoration,
                fontSize = with (typography) {
                    when (state.headingLevel) {
                        1 -> displayLarge.fontSize
                        2 -> displayMedium.fontSize
                        3 -> displaySmall.fontSize
                        4 -> headlineSmall.fontSize
                        5 -> titleLarge.fontSize
                        6 -> titleSmall.fontSize
                        else -> textStyle.fontSize
                    }
                },
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
