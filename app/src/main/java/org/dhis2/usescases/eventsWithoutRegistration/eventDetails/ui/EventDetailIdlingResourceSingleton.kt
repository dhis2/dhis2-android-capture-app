package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.test.espresso.idling.CountingIdlingResource

object EventDetailIdlingResourceSingleton {

    private const val RESOURCE = "EVENT_DETAIL"

    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
