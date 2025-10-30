package org.dhis2.usescases.notes

import androidx.test.espresso.idling.CountingIdlingResource

object NotesIdlingResource {
    private const val RESOURCE = "NOTES"

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
