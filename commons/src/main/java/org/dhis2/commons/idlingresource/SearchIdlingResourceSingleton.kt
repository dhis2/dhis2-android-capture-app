package org.dhis2.commons.idlingresource

import androidx.test.espresso.idling.CountingIdlingResource

object SearchIdlingResourceSingleton {

    private const val RESOURCE = "SEARCH"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        if (countingIdlingResource.isIdleNow) {
            countingIdlingResource.increment()
        }
        countingIdlingResource.dumpStateToLogs()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
        countingIdlingResource.dumpStateToLogs()
    }
}
