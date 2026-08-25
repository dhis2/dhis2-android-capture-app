package org.dhis2.mobile.commons.session

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.dhis2.mobile.commons.error.DomainError
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val SIGNAL_TIMEOUT = 100L

class SessionRenewalNotifierTest {
    private val notifier = SessionRenewalNotifier()

    @Test
    fun `GIVEN a session renewal error WHEN it is passed to the guard THEN the signal is raised`() =
        runTest {
            // WHEN
            notifier.notifyIfRequired(DomainError.SessionRenewalRequiredError("expired"))

            // THEN
            assertNotNull(awaitSignal())
        }

    @Test
    fun `GIVEN another error WHEN it is passed to the guard THEN nothing is raised`() =
        runTest {
            // GIVEN - a failure the app can retry on its own, or no failure at all
            notifier.notifyIfRequired(DomainError.NetworkError("offline"))
            notifier.notifyIfRequired(null)

            // THEN - the user is not sent through a login they do not need
            assertNull(awaitSignal())
        }

    @Test
    fun `GIVEN a signal raised while nobody observes WHEN a collector starts THEN it still arrives`() =
        runTest {
            // GIVEN - raised by a background sync, with no screen listening
            notifier.notifyRenewalRequired()

            // THEN - the user is asked once the app is in front of them again
            assertNotNull(awaitSignal())
        }

    @Test
    fun `GIVEN a signal already acted on WHEN a collector starts THEN it is not replayed`() =
        runTest {
            // GIVEN
            notifier.notifyRenewalRequired()

            // WHEN - the dialog was shown and the decision taken
            notifier.consume()

            // THEN - the user is not asked twice for the same expiry
            assertNull(awaitSignal())
        }

    private suspend fun awaitSignal() =
        withTimeoutOrNull(SIGNAL_TIMEOUT) {
            notifier.renewalRequired.first()
        }
}
