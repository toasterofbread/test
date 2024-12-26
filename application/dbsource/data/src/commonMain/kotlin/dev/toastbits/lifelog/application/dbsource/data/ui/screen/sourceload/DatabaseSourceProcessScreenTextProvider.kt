package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.runtime.Composable
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.data.generated.resources.database_processor_button_cancel
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

interface DatabaseSourceProcessScreenTextProvider {
    @Composable
    fun getFinishedText(warnings: List<Alert>, errors: List<Alert>, duration: Duration): String

    @Composable
    fun getProcessingTooltip(): String

    @Composable
    fun getProceedButton(): String

    @Composable
    fun getCancelButton(): String =
        stringResource(Res.string.database_processor_button_cancel)
}
