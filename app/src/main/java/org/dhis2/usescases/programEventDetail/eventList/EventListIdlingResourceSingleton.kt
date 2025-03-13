package org.dhis2.usescases.programEventDetail.eventList

import androidx.test.espresso.idling.CountingIdlingResource

object EventListIdlingResourceSingleton {

    private const val RESOURCE = "EventList"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

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
