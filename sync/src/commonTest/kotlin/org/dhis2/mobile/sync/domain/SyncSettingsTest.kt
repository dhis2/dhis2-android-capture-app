package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SyncPeriod
import org.junit.Test
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SyncSettingsTest {
    private val syncRepository: SyncRepository = mock()
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()

    private val syncSettings = SyncSettings(syncRepository, syncBackgroundJobAction)

    @Test
    fun `Should not cancel sync settings if period does not change to manual`() =
        runBlocking {
            whenever(syncRepository.currentMetadataSyncPeriod()) doReturnConsecutively
                listOf(
                    SyncPeriod.Every24Hour,
                    SyncPeriod.Every12Hour,
                )

            syncSettings.invoke()

            verify(
                syncBackgroundJobAction,
                never(),
            ).launchMetadataSync(SyncPeriod.Every12Hour.toSeconds())
            verify(syncBackgroundJobAction, never()).cancelSyncSettings()
        }

    @Test
    fun `Should cancel sync settings if period changes from manual`() =
        runBlocking {
            whenever(syncRepository.currentMetadataSyncPeriod()) doReturnConsecutively
                listOf(
                    SyncPeriod.Manual,
                    SyncPeriod.Every12Hour,
                )

            syncSettings.invoke()

            verify(
                syncBackgroundJobAction,
                times(1),
            ).launchMetadataSync(SyncPeriod.Every12Hour.toSeconds())
            verify(syncBackgroundJobAction, times(1)).cancelSyncSettings()
        }
}
