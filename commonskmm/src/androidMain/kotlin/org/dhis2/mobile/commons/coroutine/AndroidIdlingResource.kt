package org.dhis2.mobile.commons.coroutine

import androidx.test.espresso.idling.CountingIdlingResource

object AndroidIdlingResource : CoroutineIdlingResource {
    private const val RESOURCE = "MULTIPLATFORM_IDLING_RESOURCE"
    private val countingIdlingResource = CountingIdlingResource(RESOURCE)

    private val lock = Any()

    override fun increment() =
        synchronized(lock) {
            if (countingIdlingResource.isIdleNow) {
                countingIdlingResource.increment()
            }
            countingIdlingResource.dumpStateToLogs()
        }

    override fun unconditionalIncrement() =
        synchronized(lock) {
            countingIdlingResource.increment()
            countingIdlingResource.dumpStateToLogs()
        }

    override fun decrement() =
        synchronized(lock) {
            if (!countingIdlingResource.isIdleNow) {
                countingIdlingResource.decrement()
            }
            countingIdlingResource.dumpStateToLogs()
        }

    override fun unconditionalDecrement() =
        synchronized(lock) {
            countingIdlingResource.decrement()
            countingIdlingResource.dumpStateToLogs()
        }

    fun getIdlingResource() = countingIdlingResource
}
