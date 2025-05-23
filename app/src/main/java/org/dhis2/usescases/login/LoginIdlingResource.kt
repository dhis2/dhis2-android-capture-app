package org.dhis2.usescases.login

import androidx.test.espresso.idling.CountingIdlingResource

object LoginIdlingResource {
    private const val RESOURCE = "LOGIN"

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
