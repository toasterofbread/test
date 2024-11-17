package util

import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Task
import org.gradle.kotlin.dsl.withType

inline fun <reified T: Task> NamedDomainObjectSet<Task>.withNames(
    vararg names: String,
    crossinline action: T.() -> Unit
) {
    withType<T> {
        if (!names.contains(name)) {
            return@withType
        }
        action()
    }
}

inline fun <reified T: Task> NamedDomainObjectSet<Task>.buildTasks(
    releaseTaskName: String,
    debugTaskName: String,
    crossinline action: T.(isDebug: Boolean) -> Unit
) {
    withType<T> {
        val isDebug: Boolean =
            if (name == releaseTaskName) false
            else if (name == debugTaskName) true
            else return@withType
        action(isDebug)
    }
}
