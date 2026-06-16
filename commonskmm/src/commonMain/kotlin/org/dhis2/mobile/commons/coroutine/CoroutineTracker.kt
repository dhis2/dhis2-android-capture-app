package org.dhis2.mobile.commons.coroutine

import kotlinx.atomicfu.atomic

object CoroutineTracker {
    private val activeTasks = atomic(0)

    fun increment() {
        IdlingResourceProvider.idlingResource.increment()
    }

    fun decrement() {
        var current: Int
        var next: Int
        do {
            current = activeTasks.value
            next = (current - 1).coerceAtLeast(0)
        } while (!activeTasks.compareAndSet(current, next))
        IdlingResourceProvider.idlingResource.decrement()
    }
}
