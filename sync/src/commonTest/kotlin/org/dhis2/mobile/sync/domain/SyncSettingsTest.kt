package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SyncPeriod
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SyncSettingsTest {
    private val syncRepository: SyncRepository = mock()
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()

    private val syncSettings = SyncSettings(syncRepository, syncBackgroundJobAction)

    @Test
    fun `Should cancel sync settings job if both metadata and data sync are manual`() =
        runBlocking {
            whenever(syncRepository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(syncRepository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual

            syncSettings.invoke()

            verify(syncBackgroundJobAction).cancelSyncSettings()
        }

    @Test
    fun `Should not cancel sync settings if metadata is not manual`() =
        runBlocking {
            whenever(syncRepository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Every24Hour
            whenever(syncRepository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual

            syncSettings.invoke()

            verify(syncBackgroundJobAction, never()).cancelSyncSettings()
        }

    @Test
    fun `Should not cancel sync settings if data is not manual`() =
        runBlocking {
            whenever(syncRepository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(syncRepository.currentDataSyncPeriod()) doReturn SyncPeriod.Every24Hour

            syncSettings.invoke()

            verify(syncBackgroundJobAction, never()).cancelSyncSettings()
        }

    @Test
    fun `Should not cancel sync settings if both are not manual`() =
        runBlocking {
            whenever(syncRepository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Every24Hour
            whenever(syncRepository.currentDataSyncPeriod()) doReturn SyncPeriod.Every24Hour

            syncSettings.invoke()

            verify(syncBackgroundJobAction, never()).cancelSyncSettings()
        }
}
