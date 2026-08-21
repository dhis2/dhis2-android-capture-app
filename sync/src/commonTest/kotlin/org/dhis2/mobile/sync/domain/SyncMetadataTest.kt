package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SMSConfigResult
import org.dhis2.mobile.sync.model.SyncPeriod
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class SyncMetadataTest {
    private val repository: SyncRepository = mock()
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()
    private val sessionRenewalNotifier: SessionRenewalNotifier = mock()

    private val syncMetadata =
        SyncMetadata(
            repository,
            syncBackgroundJobAction,
            sessionRenewalNotifier,
        )

    @Test
    fun `Should not trigger background jobs if sync periods do not change`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction, never()).launchMetadataSync(any())
            verify(syncBackgroundJobAction, never()).cancelMetadataSync()
            verify(syncBackgroundJobAction, never()).launchDataSync(any())
            verify(syncBackgroundJobAction, never()).cancelDataSync()
        }

    @Test
    fun `Should cancel metadata sync if period changes to manual`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Manual,
            )
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).cancelMetadataSync()
            verify(syncBackgroundJobAction).launchSyncSettings()
        }

    @Test
    fun `Should re-launch metadata sync if period changes`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Every7Days,
            )
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).launchMetadataSync(SyncPeriod.Every7Days.toSeconds())
        }

    @Test
    fun `Should cancel data sync if period changes to manual`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Manual,
            )
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).cancelDataSync()
        }

    @Test
    fun `Should re-launch data sync if period changes`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Every7Days,
            )
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).launchDataSync(SyncPeriod.Every7Days.toSeconds())
        }

    @Test
    fun `Should return failure and save state when metadata sync fails`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            val exception = Exception("Sync failed")
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.failure(exception)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            val result = syncMetadata.invoke { }

            verify(repository).saveMetadataSyncState(false)
            assert(result.isFailure)
            assert(result.exceptionOrNull() == exception)
        }

    @Test
    fun `Should request a session renewal when metadata cannot be synced with an expired session`() =
        runBlocking {
            // GIVEN - the tokens can no longer be refreshed, so the metadata call never lands
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.failure(error)

            val result = syncMetadata.invoke { }

            // THEN - the failure is reported as before and the user is asked to log in again
            assertEquals(error, result.exceptionOrNull())
            verify(sessionRenewalNotifier).notifyIfRequired(error)
        }

    @Test
    fun `Should not request a session renewal when metadata syncs`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(sessionRenewalNotifier, never()).notifyRenewalRequired()
        }

    @Test
    fun `Should request a session renewal when the server check reports an expired session`() =
        runBlocking {
            // GIVEN - the ping itself fails because the tokens are gone
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            whenever(repository.isServerAvailable(any())).thenAnswer { throw error }

            // WHEN
            val result = syncMetadata.invoke { }

            // THEN - the use case reports it instead of letting it escape into the worker
            assertEquals(error, result.exceptionOrNull())
            verify(sessionRenewalNotifier).notifyIfRequired(error)
        }
}
