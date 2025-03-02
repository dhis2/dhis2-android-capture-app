package org.dhis2.common.idlingresources

import androidx.test.espresso.IdlingResource
import org.dhis2.mobile.commons.coroutine.CoroutineTracker

object CoroutineIdlingResource : IdlingResource {
    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName() = "KMP CoroutineIdlingResource"

    override fun isIdleNow(): Boolean {
        val idle = CoroutineTracker.isIdle()
        if (idle) {
            callback?.onTransitionToIdle()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        CoroutineIdlingResource.callback = callback
    }
}
