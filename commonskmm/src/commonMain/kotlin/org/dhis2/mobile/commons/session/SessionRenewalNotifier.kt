package org.dhis2.mobile.commons.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.dhis2.mobile.commons.error.DomainError

/**
 * Signals that the session cannot reach the server any more and only the user can recover it, by
 * logging in again. Raised where a server call reports
 * [org.dhis2.mobile.commons.error.DomainError.SessionRenewalRequiredError] and observed by the UI,
 * which asks the user whether to renew now.
 *
 * The signal is replayed to the next collector, so one raised by a background sync still reaches
 * the user when the app comes to the foreground. Call [consume] once it has been acted on.
 */
class SessionRenewalNotifier {
    private val _renewalRequired = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    val renewalRequired: Flow<Unit> = _renewalRequired.asSharedFlow()

    suspend fun notifyRenewalRequired() {
        _renewalRequired.emit(Unit)
    }

    /** Raises the signal only for the error that the user can act on. */
    suspend fun notifyIfRequired(error: Throwable?) {
        if (error is DomainError.SessionRenewalRequiredError) {
            notifyRenewalRequired()
        }
    }

    fun consume() {
        _renewalRequired.resetReplayCache()
    }
}
