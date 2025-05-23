package org.dhis2.commons.orgunitselector

import androidx.test.espresso.idling.CountingIdlingResource

object OrgUnitIdlingResource {
    private const val RESOURCE = "ORG_UNIT"

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
