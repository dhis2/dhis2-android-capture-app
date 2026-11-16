package org.dhis2.mobile.commons.coroutine

object CoroutineTracker {
    fun increment() {
        IdlingResourceProvider.idlingResource.increment()
    }

    fun unconditionalIncrement() {
        IdlingResourceProvider.idlingResource.unconditionalIncrement()
    }

    fun decrement() {
        IdlingResourceProvider.idlingResource.decrement()
    }

    fun unconditionalDecrement() {
        IdlingResourceProvider.idlingResource.unconditionalDecrement()
    }
}
