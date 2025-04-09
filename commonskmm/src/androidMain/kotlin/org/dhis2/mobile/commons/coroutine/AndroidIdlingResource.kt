package org.dhis2.mobile.commons.coroutine

import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource

object AndroidIdlingResource : CoroutineIdlingResource {
    private const val RESOURCE = "MULTIPLATFORM_IDLING_RESOURCE"
    private val countingIdlingResource = CountingIdlingResource(RESOURCE)

    override fun increment() {
        if (countingIdlingResource.isIdleNow) {
            Log.d("CoroutineIdlingResource", "Increment")
            countingIdlingResource.increment()
        }
    }

    override fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            Log.d("CoroutineIdlingResource", "Decrement")
            countingIdlingResource.decrement()
        }
    }

    fun getIdlingResource() = countingIdlingResource
}
