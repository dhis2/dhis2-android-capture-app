package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncDataTest {
    private val repository: SyncRepository = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val sessionRenewalNotifier: SessionRenewalNotifier = mock()

    private val syncData =
        SyncData(
            repository = repository,
            syncStatusController = syncStatusController,
            sessionRenewalNotifier = sessionRenewalNotifier,
        )

    @Test
    fun `GIVEN the tokens are no longer valid WHEN data is synced THEN a session renewal is requested`() =
        runTest {
            // GIVEN - the SDK cannot renew the tokens on its own, so no request reaches the server
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            whenever(repository.isServerAvailable(any())).thenAnswer { throw error }

            // WHEN
            val result = syncData { }

            // THEN - the sync still fails, but the app is told the user has to log in again:
            // nothing else in the app can recover from this on its own
            assertEquals(error, result.exceptionOrNull())
            verify(sessionRenewalNotifier).notifyRenewalRequired()
        }

    @Test
    fun `GIVEN the server is unavailable WHEN data is synced THEN no session renewal is requested`() =
        runTest {
            // GIVEN - a plain connectivity problem, which a later sync can retry
            whenever(repository.isServerAvailable(any())) doReturn false

            // WHEN
            val result = syncData { }

            // THEN - the user is not sent through a login they do not need
            assertTrue(result.isFailure)
            verify(sessionRenewalNotifier, never()).notifyRenewalRequired()
        }

    @Test
    fun `GIVEN an upload fails because the session expired WHEN data is synced THEN a renewal is requested`() =
        runTest {
            // GIVEN - the repository reports failures as results rather than throwing them, which
            // is how an expired session shows up during a data sync
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            stubSyncFlow()
            whenever(repository.uploadEvents()) doReturn Result.failure(error)

            // WHEN
            val result = syncData { }

            // THEN - the sync itself completes, and the user is told they have to log in again
            // instead of the expired session being recorded as a plain sync failure
            assertTrue(result.isSuccess)
            verify(repository).saveDataSyncState(false)
            verify(sessionRenewalNotifier).notifyIfRequired(error)
        }

    @Test
    fun `GIVEN every step succeeds WHEN data is synced THEN no renewal is requested`() =
        runTest {
            // GIVEN
            stubSyncFlow()

            // WHEN
            val result = syncData { }

            // THEN
            assertTrue(result.isSuccess)
            verify(repository).saveDataSyncState(true)
            verify(sessionRenewalNotifier).notifyIfRequired(null)
            verify(sessionRenewalNotifier, never()).notifyRenewalRequired()
        }

    private suspend fun stubSyncFlow() {
        whenever(repository.isServerAvailable(any())) doReturn true
        whenever(repository.getAllProgramsInitialStatus()) doReturn Result.success(emptyMap())
        whenever(repository.uploadEvents()) doReturn Result.success(Unit)
        whenever(repository.downloadEvents(any())) doReturn Result.success(Unit)
        whenever(repository.getAllEventPrograms()) doReturn Result.success(emptyList())
        whenever(repository.uploadTEIs()) doReturn Result.success(Unit)
        whenever(repository.downloadTEIs(any())) doReturn Result.success(Unit)
        whenever(repository.getAllTrackerPrograms()) doReturn Result.success(emptyList())
        whenever(repository.uploadDataValues()) doReturn Result.success(Unit)
        whenever(repository.downloadDataValues(any())) doReturn Result.success(Unit)
        whenever(repository.getAllDataSets()) doReturn Result.success(emptyList())
        whenever(repository.downloadDataFileResources(any())) doReturn Result.success(Unit)
        whenever(repository.downloadReservedValues(any())) doReturn Result.success(Unit)
        whenever(repository.saveDataSyncState(any())) doReturn Result.success(Unit)
    }
}
