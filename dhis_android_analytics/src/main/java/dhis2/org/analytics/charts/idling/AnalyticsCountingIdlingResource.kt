package dhis2.org.analytics.charts.idling

import androidx.test.espresso.idling.CountingIdlingResource

object AnalyticsCountingIdlingResource {
    private const val RESOURCE = "ANALYTICS"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
