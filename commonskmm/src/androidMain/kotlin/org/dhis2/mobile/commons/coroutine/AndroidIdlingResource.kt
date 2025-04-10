package org.dhis2.mobile.commons.coroutine

import androidx.test.espresso.idling.CountingIdlingResource

object AndroidIdlingResource : CoroutineIdlingResource {
    private const val RESOURCE = "MULTIPLATFORM_IDLING_RESOURCE"
    private val countingIdlingResource = CountingIdlingResource(RESOURCE)

    override fun increment() {
        if (countingIdlingResource.isIdleNow) {
            countingIdlingResource.increment()
        }
        countingIdlingResource.dumpStateToLogs()
    }

    override fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
        countingIdlingResource.dumpStateToLogs()
    }

    fun getIdlingResource() = countingIdlingResource
}
