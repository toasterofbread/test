package dev.toastbits.lifelog.application.logview.manager

import dev.toastbits.lifelog.application.logview.model.LogEntityChanges
import dev.toastbits.lifelog.application.logview.model.LogEventReference
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal abstract class LogDatabaseChangesManager(
    private val defaultEvent: LogEvent,
    private val coroutineScope: CoroutineScope,
    private val eventChanges: MutableMap<LogEventReference, LogEntityChanges<LogEvent>> = mutableMapOf()
): Map<LogEventReference, LogEntityChanges<LogEvent>> by eventChanges {
    private val queuedChangeJobs: MutableMap<LogEventReference, Job> = mutableMapOf()
    private val lock: Mutex = Mutex()
    private val queueLock: Mutex = Mutex()
    private object ApplyImmediatelyException: CancellationException(null)

    protected abstract fun getOriginalLogEvent(eventReference: LogEventReference): LogEvent?
    protected abstract fun onChangeRemoved()

    fun remove(key: LogEventReference) = launchWithLock {
        queuedChangeJobs[key]?.cancel()
        eventChanges.remove(key)

        if (getOriginalLogEvent(key) == null) {
            eventChanges.shiftDateEventsDown(key)
            queuedChangeJobs.shiftDateEventsDown(key) { job, newReference ->
                (job as CoroutineScope).coroutineContext[EventReferenceElement.Key]!!.reference = newReference
            }
        }

        onChangeRemoved()
    }

    private inline fun <V> MutableMap<LogEventReference, V>.shiftDateEventsDown(
        from: LogEventReference,
        migrateValue: (V, LogEventReference) -> Unit = { _, _ -> }
    ) {
        val iterator: MutableIterator<MutableMap.MutableEntry<LogEventReference, V>> = iterator()
        val removed: MutableMap<LogEventReference, V> = mutableMapOf()

        while (iterator.hasNext()) {
            val (ref, value) = iterator.next()
            if (ref.date == from.date && ref.logIndex > from.logIndex) {
                iterator.remove()
                removed[ref] = value
            }
        }

        for ((ref, value) in removed) {
            val newRef: LogEventReference = ref.copy(logIndex = ref.logIndex - 1)
            migrateValue(value, newRef)
            set(newRef, value)
        }
    }

    fun clear() = launchWithLock {
        for (job in queuedChangeJobs.values) {
            job.cancel()
        }
        eventChanges.clear()
        onChangeRemoved()
    }

    suspend fun applyAllQueuedChanges() {
        val jobs: Collection<Job> =
            lock.withLock {
                queuedChangeJobs.values
            }
        for (job in jobs) {
            job.cancel(ApplyImmediatelyException)
        }
        jobs.joinAll()
    }

    suspend fun applyToDatabase(logDatabase: LogDatabase): LogDatabase {
        applyAllQueuedChanges()
        queueLock.withLock {
            return logDatabase.copy(
                days = logDatabase.days.toMutableMap().also { days ->
                    for ((ref, changes) in eventChanges.entries.sortedBy { it.key }) {
                        val events: List<LogEvent> = days[ref.date].orEmpty()
                        days[ref.date] = events.toMutableList().apply {
                            val newEvent: LogEvent = changes.applyTo(getOrNull(ref.logIndex))
                            if (ref.logIndex < size) {
                                set(ref.logIndex, newEvent)
                            }
                            else {
                                add(newEvent)
                            }
                        }
                    }
                }
            )
        }
    }

    suspend fun applyNewChanges(
        eventReference: LogEventReference,
        changes: LogEntityChanges<LogEvent>
    ) {
        lock.withLock {
            applyChanges(changes, eventReference)
        }
    }

    fun queueNewChanges(
        eventReference: LogEventReference,
        queuedChanges: LogDatabaseQueuedChanges
    ) {
        check(coroutineScope.isActive) {
            "Trying to queue a log change with a dead CoroutineScope"
        }

        coroutineScope.launch(EventReferenceElement(eventReference)) {
            queueLock.withLock {
                val currentChanges: LogEntityChanges<LogEvent>? =
                    lock.withLock {
                        queuedChangeJobs[eventReference]?.cancel()
                        queuedChangeJobs[eventReference] = coroutineContext.job
                        return@withLock eventChanges[eventReference]
                    }

                if (currentChanges != null) {
                    try {
                        delay(queuedChanges.delay)
                    }
                    catch (_: ApplyImmediatelyException) {}
                }

                val newChanges: LogEntityChanges<LogEvent> =
                    try {
                        queuedChanges.loadChanges(currentChanges ?: LogEntityChanges.createEmpty(defaultEvent))
                    }
                    catch (_: ApplyImmediatelyException) {
                        withContext(NonCancellable) {
                            queuedChanges.loadChanges(currentChanges ?: LogEntityChanges.createEmpty(defaultEvent))
                        }
                    }

                lock.withLock {
                    applyChanges(newChanges, coroutineContext[EventReferenceElement.Key]!!.reference)
                }
            }
        }
    }

    private data class EventReferenceElement(
        var reference: LogEventReference
    ): AbstractCoroutineContextElement(Key) {
        companion object Key: CoroutineContext.Key<EventReferenceElement>
    }

    private fun applyChanges(
        changes: LogEntityChanges<LogEvent>,
        eventReference: LogEventReference,
    ) {
        if (changes.hasChanges(getOriginalLogEvent(eventReference))) {
            eventChanges[eventReference] = changes
        }
        else {
            eventChanges.remove(eventReference)
        }
    }

    private fun launchWithLock(action: suspend () -> Unit) {
        coroutineScope.launch {
            lock.withLock {
                action()
            }
        }
    }
}
