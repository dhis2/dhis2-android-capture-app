package org.dhis2.usescases.eventsWithoutRegistration

import androidx.test.espresso.idling.CountingIdlingResource

object EventIdlingResourceSingleton {
    private const val RESOURCE = "Event"

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
