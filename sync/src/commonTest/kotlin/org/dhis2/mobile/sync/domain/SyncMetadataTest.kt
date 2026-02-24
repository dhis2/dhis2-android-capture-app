package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SyncPeriod
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SyncMetadataTest {
    private val repository: SyncRepository = mock()
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()

    private val syncMetadata =
        SyncMetadata(
            repository,
            syncBackgroundJobAction,
        )

    @Test
    fun `Should not trigger background jobs if sync periods do not change`() =
        runBlocking {
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
            whenever(repository.currentMetadataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Manual,
            )
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).cancelMetadataSync()
            verify(syncBackgroundJobAction).launchSyncSettings()
        }

    @Test
    fun `Should re-launch metadata sync if period changes`() =
        runBlocking {
            whenever(repository.currentMetadataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Every7Days,
            )
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).launchMetadataSync(SyncPeriod.Every7Days.toSeconds())
        }

    @Test
    fun `Should cancel data sync if period changes to manual`() =
        runBlocking {
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Manual,
            )
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).cancelDataSync()
        }

    @Test
    fun `Should re-launch data sync if period changes`() =
        runBlocking {
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Every7Days,
            )
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).launchDataSync(SyncPeriod.Every7Days.toSeconds())
        }

    @Test
    fun `Should return failure and save state when metadata sync fails`() =
        runBlocking {
            val exception = Exception("Sync failed")
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.failure(exception)

            val result = syncMetadata.invoke { }

            verify(repository).saveMetadataSyncState(false)
            assert(result.isFailure)
            assert(result.exceptionOrNull() == exception)
        }
}
