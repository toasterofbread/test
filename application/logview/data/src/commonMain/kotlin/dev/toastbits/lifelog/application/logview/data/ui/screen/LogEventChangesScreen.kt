package dev.toastbits.lifelog.application.logview.data.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.bottom
import dev.toastbits.composekit.util.copy
import dev.toastbits.composekit.util.getContrasted
import dev.toastbits.lifelog.application.core.ui.GenericTopBar
import dev.toastbits.lifelog.application.logview.data.ui.component.LogEventChangesDiff
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventChanges
import dev.toastbits.lifelog.application.logview.data.ui.model.LogEventReference
import dev.toastbits.lifelog.application.logview.data.ui.model.get
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import lifelog.application.logview.data.generated.resources.Res
import lifelog.application.logview.data.generated.resources.log_view_screen_button_discard_event_changes
import lifelog.application.logview.data.generated.resources.log_view_screen_title_review_changes
import org.jetbrains.compose.resources.stringResource

class LogEventChangesScreen(
    val eventReference: LogEventReference,
    private val eventChanges: LogEventChanges,
    private val logDatabase: LogDatabase,
    private val onDiscardChanges: (() -> Unit)?
): Screen {
    @Composable
    override fun Content(navigator: Navigator, modifier: Modifier, contentPadding: PaddingValues) {
        val theme: ThemeValues = LocalComposeKitTheme.current
        val density: Density = LocalDensity.current
        val event: LogEvent = logDatabase[eventReference]

        Column(
            modifier.padding(contentPadding.copy(bottom = 0.dp)),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GenericTopBar(
                title = stringResource(Res.string.log_view_screen_title_review_changes),
                onBack = {
                    navigator.navigateBackward()
                }
            )

            Box {
                var discardButtonHeight: Dp by remember { mutableStateOf(0.dp) }

                LogEventChangesDiff(
                    event,
                    eventReference,
                    eventChanges,
                    logDatabase,
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom =
                            if (onDiscardChanges != null) discardButtonHeight
                            else contentPadding.bottom
                    ),
                    scrollBarContentPadding = PaddingValues(
                        bottom = contentPadding.bottom
                    )
                )

                if (onDiscardChanges != null) {
                    Button(
                        onDiscardChanges,
                        Modifier
                            .align(Alignment.BottomStart)
                            .onSizeChanged {
                                with (density) {
                                    discardButtonHeight = it.height.toDp()
                                }
                            }
                            .padding(bottom = contentPadding.bottom),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.error,
                            contentColor = theme.error.getContrasted()
                        )
                    ) {
                        Text(stringResource(Res.string.log_view_screen_button_discard_event_changes))
                    }
                }
            }
        }
    }
}
