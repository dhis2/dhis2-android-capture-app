package org.dhis2.mobile.commons.coroutine

import kotlinx.atomicfu.atomic

object CoroutineTracker {
    private val activeTasks = atomic(0)

    fun increment() {
        activeTasks.incrementAndGet()
    }

    fun decrement() {
        activeTasks.decrementAndGet()
    }

    fun isIdle(): Boolean = activeTasks.value == 0
}
