package org.dhis2.commons.idlingresource

import androidx.test.espresso.idling.CountingIdlingResource

object SearchIdlingResourceSingleton {

    private const val RESOURCE = "SEARCH"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE, true)

    fun increment() {
        if (countingIdlingResource.isIdleNow) {
            countingIdlingResource.increment()
        }
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
