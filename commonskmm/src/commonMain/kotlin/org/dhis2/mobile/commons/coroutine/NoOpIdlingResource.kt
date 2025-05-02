package org.dhis2.mobile.commons.coroutine

object NoOpIdlingResource : CoroutineIdlingResource {
    override fun increment() {
        // No-op: Do nothing
    }

    override fun decrement() {
        // No-op: Do nothing
    }
}
