package org.dhis2.mobile.commons.coroutine

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CoroutineTracker {
    private val activeTasks = atomic(0)
    private val _isIdle = MutableStateFlow(true)

    val isIdle: StateFlow<Boolean> get() = _isIdle

    fun increment() {
        val count = activeTasks.incrementAndGet()
        _isIdle.value = false
        IdlingResourceProvider.idlingResource.increment()
    }

    fun decrement() {
        val count = activeTasks.decrementAndGet()
        _isIdle.value = (count == 0)
        IdlingResourceProvider.idlingResource.decrement()
    }
}
