package org.dhis2.commons.schedulers

fun interface SingleEventEnforcer {
    fun processEvent(event: () -> Unit)

    companion object
}

fun SingleEventEnforcer.Companion.get(): SingleEventEnforcer = SingleEventEnforcerImpl()

class SingleEventEnforcerImpl : SingleEventEnforcer {
    private val now: Long
        get() = System.currentTimeMillis()

    private var lastEventTimeMs: Long = 0

    override fun processEvent(event: () -> Unit) {
        if (now - lastEventTimeMs >= 1200L) {
            event.invoke()
        }
        lastEventTimeMs = now
    }
}
