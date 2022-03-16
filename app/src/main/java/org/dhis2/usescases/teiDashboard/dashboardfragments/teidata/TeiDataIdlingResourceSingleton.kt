package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import androidx.test.espresso.idling.CountingIdlingResource

object TeiDataIdlingResourceSingleton {

    private const val RESOURCE = "TEI_DATA"

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
