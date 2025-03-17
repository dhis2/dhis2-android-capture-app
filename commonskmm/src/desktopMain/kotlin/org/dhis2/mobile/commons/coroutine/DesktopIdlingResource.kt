package org.dhis2.mobile.commons.coroutine

object DesktopIdlingResource : CoroutineIdlingResource {
    private var activeTasks = 0

    override fun increment() {
        synchronized(this) { activeTasks++ }
    }

    override fun decrement() {
        synchronized(this) {
            if (activeTasks > 0) {
                activeTasks--
            }
        }
    }

    fun isIdle(): Boolean = synchronized(this) { activeTasks == 0 }
}

actual val idlingResource: CoroutineIdlingResource = DesktopIdlingResource
