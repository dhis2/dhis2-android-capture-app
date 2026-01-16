package org.dhis2.form.ui.idling

import androidx.test.espresso.idling.CountingIdlingResource

object FormCountingIdlingResource {
    private const val RESOURCE = "FORM"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    @Synchronized
    fun increment() {
        countingIdlingResource.increment()
    }

    @Synchronized
    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
