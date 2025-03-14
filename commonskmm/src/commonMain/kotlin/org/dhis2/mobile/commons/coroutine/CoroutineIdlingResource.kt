package org.dhis2.mobile.commons.coroutine

interface CoroutineIdlingResource {
    fun increment()
    fun decrement()
}

object DefaultIdlingResource : CoroutineIdlingResource {
    override fun increment() {}
    override fun decrement() {}
}

expect val idlingResource: CoroutineIdlingResource
