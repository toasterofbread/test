package dev.toastbits.lifelog.application.logview.manager

import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.application.logview.model.get
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration

class LogDatabaseChangesManager(
    private val database: LogDatabase,
    private val coroutineScope: CoroutineScope,
    private val eventChanges: MutableMap<LogEventReference, LogEntityChanges<LogEvent>> = mutableMapOf()
): Map<LogEventReference, LogEntityChanges<LogEvent>> by eventChanges {
    private val queuedChangeJobs: MutableMap<LogEventReference, Job> = mutableMapOf()
    private val lock: Mutex = Mutex()

    fun remove(key: LogEventReference) = launchWithLock {
        eventChanges.remove(key)
        queuedChangeJobs[key]?.cancel()
    }

    fun clear() = launchWithLock {
        eventChanges.clear()
        for (job in queuedChangeJobs.values) {
            job.cancel()
        }
    }

    private object ApplyImmediatelyException: CancellationException(null)

    fun applyAllQueuedChanges() = launchWithLock {
        for (job in queuedChangeJobs.values) {
            job.cancel(ApplyImmediatelyException)
        }
    }

    private fun launchWithLock(action: suspend () -> Unit) {
        coroutineScope.launch {
            lock.withLock {
                action()
            }
        }
    }

    fun queueNewChanges(
        eventReference: LogEventReference,
        queuedChanges: QueuedChanges
    ) {
        check(coroutineScope.isActive) {
            "Trying to queue a log change with a dead CoroutineScope"
        }

        coroutineScope.launch {
            val currentChanges: LogEntityChanges<LogEvent>? =
                lock.withLock {
                    queuedChangeJobs[eventReference]?.cancel()
                    queuedChangeJobs[eventReference] = coroutineContext.job
                    return@withLock eventChanges[eventReference]
                }

            try {
                delay(queuedChanges.delay)
            }
            catch (_: ApplyImmediatelyException) {}

            val newChanges: LogEntityChanges<LogEvent> =
                try {
                    queuedChanges.loadChanges(currentChanges ?: LogEntityChanges.createEmpty())
                }
                catch (_: ApplyImmediatelyException) {
                    withContext(NonCancellable) {
                        queuedChanges.loadChanges(currentChanges ?: LogEntityChanges.createEmpty())
                    }
                }

            lock.withLock {
                applyChanges(newChanges, eventReference)
            }
        }
    }

    private fun applyChanges(
        changes: LogEntityChanges<LogEvent>,
        eventReference: LogEventReference,
    ) {
        if (changes.hasChanges(database[eventReference])) {
            eventChanges[eventReference] = changes
        }
        else {
            eventChanges.remove(eventReference)
        }
    }

    class QueuedChanges(
        val delay: Duration,
        val loadChanges: suspend (currentChanges: LogEntityChanges<LogEvent>) -> LogEntityChanges<LogEvent>
    )
}
