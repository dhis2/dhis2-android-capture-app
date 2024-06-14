package org.dhis2.commons.schedulers

import timber.log.Timber

fun interface SingleEventEnforcer {
    fun processEvent(event: () -> Unit)

    companion object
}

fun SingleEventEnforcer.Companion.get(): SingleEventEnforcer =
    SingleEventEnforcerImpl()

class SingleEventEnforcerImpl : SingleEventEnforcer {
    private val now: Long
        get() = System.currentTimeMillis()

    private var lastEventTimeMs: Long = 0

    override fun processEvent(event: () -> Unit) {
        if (now - lastEventTimeMs >= 1200L) {
            Timber.d(
                "should be first event and the condition is true ",
            )
            event.invoke()
        } else {
            Timber.d(
                " consecutive events and the condition is false ",
            )
        }
        lastEventTimeMs = now
    }
}
