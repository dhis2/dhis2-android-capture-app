package org.dhis2.mobile.commons.coroutine

interface CoroutineIdlingResource {
    fun increment()

    fun decrement()
}
