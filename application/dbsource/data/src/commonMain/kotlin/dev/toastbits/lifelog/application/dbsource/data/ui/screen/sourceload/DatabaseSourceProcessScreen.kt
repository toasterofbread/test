package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.composekit.util.platform.launchSingle
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.util.rememberDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

abstract class DatabaseSourceProcessScreen<R>(
    private val sourceConfiguration: DatabaseSourceConfiguration,
    private val textProvider: DatabaseSourceProcessScreenTextProvider,
    private val autoProceed: Boolean = false,
    private val showProceedAndCancel: Boolean = true
): Screen {
    private var loadJob: Job? = null
    private val finishedStepsProgress: MutableList<DatabaseAccessor.LoadProgress> = mutableStateListOf()
    private var currentProgress: DatabaseAccessor.LoadProgress? by mutableStateOf(null)
    private var loadResult: Result<Pair<R, Duration>>? by mutableStateOf(null)

    internal abstract fun getInitialStep(databaseAccessor: DatabaseAccessor): LoadStep<R>

    protected abstract fun canProceedWithResult(result: R): Boolean
    protected abstract fun getResultAlerts(result: R): List<Alert>

    protected open fun hasUserProceedAction(): Boolean = false
    protected open fun onUserProceeded(result: R) {}

    protected open fun hasRetryAction(): Boolean = false
    protected open suspend fun onRetry() {}

    protected open fun onProcessFinished(result: R) {}

    override val canNavigateBackwardFrom: Boolean
        get() = loadResult != null

    @Composable
    override fun Content(modifier: Modifier, contentPadding: PaddingValues) {
        val coroutineScope: CoroutineScope = rememberCoroutineScope()
        val databaseAccessor: DatabaseAccessor = rememberDatabaseAccessor(sourceConfiguration)

        LaunchedEffect(Unit) {
            if (loadResult == null && loadJob == null) {
                coroutineScope.startLoad(databaseAccessor)
            }
        }

        val result: Pair<R, Duration>? = loadResult?.getOrNull()

        DatabaseSourceProcessor(
            sourceConfiguration = sourceConfiguration,
            databaseAccessor = databaseAccessor,
            textProvider = textProvider,
            loadException = loadResult?.exceptionOrNull(),
            finishedStepsProgress = finishedStepsProgress,
            currentProgress = currentProgress,
            loadResult = result,
            alerts = remember(result) { result?.first?.let { getResultAlerts(it) }.orEmpty() },
            modifier = modifier.padding(contentPadding),
            onRetry = {
                coroutineScope.startLoad(databaseAccessor)
            },
            onUserProceeded = ::onUserProceeded.takeIf { hasUserProceedAction() },
            autoProceed = autoProceed,
            canProceedWith = ::canProceedWithResult,
            showProceedAndCancel = showProceedAndCancel,
            cancel = ::cancel
        )
    }

    private suspend fun cancel(navigator: Navigator) {
        loadJob?.cancelAndJoin()
        navigator.navigateBackward()
    }

    private fun CoroutineScope.startLoad(databaseAccessor: DatabaseAccessor) {
        loadJob = launchSingle {
            loadResult = null
            finishedStepsProgress.clear()
            currentProgress = null
            loadResult =
                continueLoad(
                    step = getInitialStep(databaseAccessor),
                    databaseAccessor = databaseAccessor,
                    startTime = TimeSource.Monotonic.markNow()
                )
                .onSuccess { (result) ->
                    onProcessFinished(result)

                    if (autoProceed && canProceedWithResult(result)) {
                        onUserProceeded(result)
                    }
                }
                .onFailure { error ->
                    error.printStackTrace()
                }
            loadJob = null
        }
    }

    private suspend fun continueLoad(
        step: LoadStep<R>,
        databaseAccessor: DatabaseAccessor,
        startTime: TimeMark
    ): Result<Pair<R, Duration>> = runCatching {
        val result: LoadStep.ExecuteResult<R> =
            step.execute(databaseAccessor) { progress ->
                if (progress.isError) {
                    finishedStepsProgress.add(progress)
                    return@execute
                }

                val current: DatabaseAccessor.LoadProgress? = currentProgress
                if (current != null && (current.isUnique() || current.getMessageResource() != progress.getMessageResource())) {
                    finishedStepsProgress.add(current)
                }
                currentProgress = progress
            }

        currentProgress?.also {
            finishedStepsProgress.add(it)
            currentProgress = null
        }

        when (result) {
            is LoadStep.ExecuteResult.Done -> {
                return@runCatching result.result to startTime.elapsedNow()
            }
            is LoadStep.ExecuteResult.ExceptionThrown -> {
                throw result.exception
            }
            is LoadStep.ExecuteResult.NextStep -> {
                return continueLoad(result.nextStep, databaseAccessor, startTime)
            }
        }
    }
}
