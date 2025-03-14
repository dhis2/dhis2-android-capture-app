package org.dhis2.mobile.commons.coroutine

import androidx.test.espresso.idling.CountingIdlingResource

object AndroidIdlingResource : CoroutineIdlingResource {
    private const val RESOURCE = "MULTIPLATFORM_IDLING_RESOURCE"
    private val countingIdlingResource = CountingIdlingResource(RESOURCE)

    override fun increment() {
        countingIdlingResource.increment()
    }

    override fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }

    fun getIdlingResource() = countingIdlingResource
}

// Provide the actual instance for Android
actual val idlingResource: CoroutineIdlingResource = AndroidIdlingResource
