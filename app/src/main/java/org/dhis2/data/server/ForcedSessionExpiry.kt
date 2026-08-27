package org.dhis2.data.server

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Arms [ForceSessionExpiryInterceptor] for one session, from the development screen.
 *
 * It is deliberately in memory and single use: arming spoils the next session that reaches the
 * server, and the SDK then discards the tokens, so the account stays expired until the user renews
 * it. Nothing is persisted, so no build can be left in this state by accident.
 */
object ForcedSessionExpiry {
    private val armed = AtomicBoolean(false)

    val isArmed: Boolean
        get() = armed.get()

    fun arm() {
        armed.set(true)
    }

    fun disarm() {
        armed.set(false)
    }
}
