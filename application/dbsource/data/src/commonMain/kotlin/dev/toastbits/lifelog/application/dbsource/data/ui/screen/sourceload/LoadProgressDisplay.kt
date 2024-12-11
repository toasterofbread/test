package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import dev.toastbits.composekit.components.platform.composable.theme.ThemedLinearProgressIndicator
import dev.toastbits.composekit.components.utils.composable.animatedvisibility.NullableValueAnimatedVisibility
import dev.toastbits.composekit.theme.ThemeValues
import dev.toastbits.composekit.theme.ui.LocalComposeKitTheme
import dev.toastbits.composekit.util.rememberLocalisedValue
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoadProgressDisplay(
    progress: DatabaseAccessor.LoadProgress,
    modifier: Modifier = Modifier,
    done: Boolean = false
) {
    val theme: ThemeValues = LocalComposeKitTheme.current

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            stringResource(progress.getMessageResource()),
            style = MaterialTheme.typography.labelLarge,
            color = if (progress.isError) theme.error else theme.onBackground
        )

        val progressMessage: String? by rememberLocalisedValue({ progress.getProgressMessage() }, { null })

        NullableValueAnimatedVisibility(
            progressMessage,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) { message ->
            if (message != null) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.6f)
                )
            }
        }

        if (progress is DatabaseAccessor.LoadProgress.Absolute) {
            theme.ThemedLinearProgressIndicator({ progress.progressFraction })
        }
        else if (!done) {
            theme.ThemedLinearProgressIndicator()
        }
    }
}
