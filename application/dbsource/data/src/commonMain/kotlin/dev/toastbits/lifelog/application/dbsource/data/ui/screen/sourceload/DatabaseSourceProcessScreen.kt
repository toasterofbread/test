package dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.toastbits.composekit.navigation.navigator.Navigator
import dev.toastbits.composekit.navigation.screen.Screen
import dev.toastbits.lifelog.application.dbsource.data.ui.screen.sourceload.step.LoadStep
import dev.toastbits.lifelog.application.dbsource.data.ui.util.rememberDatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor
import dev.toastbits.lifelog.application.dbsource.domain.configuration.DatabaseSourceConfiguration
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

abstract class DatabaseSourceProcessScreen<R>(
    private val sourceConfiguration: DatabaseSourceConfiguration,
    private val textProvider: DatabaseSourceProcessScreenTextProvider,
    private val autoProceed: Boolean = false,
    private val showProceedAndCancel: Boolean = true
): Screen {
    private var started: Boolean = false
    private var currentStep: LoadStep<R>? = null
    private var loadJob: Job? = null
    private var loadException: Throwable? by mutableStateOf(null)
    private val finishedStepsProgress: MutableList<DatabaseAccessor.LoadProgress> = mutableStateListOf()
    private var currentProgress: DatabaseAccessor.LoadProgress? by mutableStateOf(null)
    private val loadStartTime: TimeMark = TimeSource.Monotonic.markNow()
    private var loadResult: Pair<R, Duration>? by mutableStateOf(null)

    private val isRunning: Boolean
        get() = !started || currentStep != null

    internal abstract fun getInitialStep(databaseAccessor: DatabaseAccessor): LoadStep<R>

    protected abstract fun canProceedWithResult(result: R): Boolean
    protected abstract fun getResultAlerts(result: R): List<Alert>

    protected open fun hasUserProceedAction(): Boolean = false
    protected open fun onUserProceeded(result: R) {}
    protected open fun onProcessFinished(result: R) {}

    override val canNavigateBackwardFrom: Boolean
        get() = !isRunning

    @Composable
    override fun Content(modifier: Modifier, contentPadding: PaddingValues) {
        val databaseAccessor: DatabaseAccessor = rememberDatabaseAccessor(sourceConfiguration)

        LaunchedEffect(Unit) {
            loadJob = launch {
                try {
                    continueLoad(databaseAccessor)
                }
                catch (e: Throwable) {
                    loadException = e
                    currentStep = null
                }
            }
        }

        DatabaseSourceProcessor(
            sourceConfiguration = sourceConfiguration,
            databaseAccessor = databaseAccessor,
            textProvider = textProvider,
            loadException = loadException,
            finishedStepsProgress = finishedStepsProgress,
            currentProgress = currentProgress,
            loadResult = loadResult,
            getAlerts = ::getResultAlerts,
            modifier = modifier.padding(contentPadding),
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

    private suspend fun continueLoad(databaseAccessor: DatabaseAccessor) {
        if (!started) {
            currentStep = getInitialStep(databaseAccessor)
            started = true
        }

        val step: LoadStep<R> = currentStep ?: return

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
                currentStep = null
                loadResult = result.result to loadStartTime.elapsedNow()

                onProcessFinished(result.result)

                if (autoProceed && canProceedWithResult(result.result)) {
                    onUserProceeded(result.result)
                }
            }
            is LoadStep.ExecuteResult.ExceptionThrown -> {
                currentStep = null
                result.exception.printStackTrace()
                loadException = result.exception
            }

            is LoadStep.ExecuteResult.NextStep -> {
                currentStep = result.nextStep
                continueLoad(databaseAccessor)
            }
        }
    }
}
