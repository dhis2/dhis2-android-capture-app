package org.dhis2.usescases.settings.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.utils.analytics.AnalyticsHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchSyncTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testingDispatcher = UnconfinedTestDispatcher()

    private lateinit var launchSync: LaunchSync
    private val workManagerController: WorkManagerController = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

    private val mockedMetadataWorkInfo = MutableLiveData<List<WorkInfo>>()
    private val mockedDataWorkInfo = MutableLiveData<List<WorkInfo>>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(workManagerController.getWorkInfosByTagLiveData(Constants.META_NOW)) doReturn mockedMetadataWorkInfo
        whenever(workManagerController.getWorkInfosByTagLiveData(Constants.DATA_NOW)) doReturn mockedDataWorkInfo
        launchSync =
            LaunchSync(
                workManagerController = workManagerController,
                preferenceProvider = preferenceProvider,
                analyticsHelper = analyticsHelper,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldStartSyncMetadata() =
        runTest {
            val expectedWorkerItem =
                WorkerItem(
                    Constants.META_NOW,
                    WorkerType.METADATA,
                    null,
                    null,
                    ExistingWorkPolicy.KEEP,
                    null,
                )
            launchSync(LaunchSync.SyncAction.SyncMetadata)
            verify(workManagerController, times(1)).syncDataForWorker(expectedWorkerItem)
        }

    @Test
    fun shouldStartSyncData() =
        runTest {
            val expectedWorkerItem =
                WorkerItem(
                    Constants.DATA_NOW,
                    WorkerType.DATA,
                    null,
                    null,
                    ExistingWorkPolicy.KEEP,
                    null,
                )
            launchSync(LaunchSync.SyncAction.SyncData)
            verify(workManagerController, times(1)).syncDataForWorker(expectedWorkerItem)
        }

    @Test
    fun shouldUpdateSyncDataPeriod() =
        runTest {
            val newPeriod = 13
            val expectedWorkerItem =
                WorkerItem(
                    Constants.DATA,
                    WorkerType.DATA,
                    newPeriod.toLong(),
                    null,
                    null,
                    ExistingPeriodicWorkPolicy.REPLACE,
                )
            launchSync(LaunchSync.SyncAction.UpdateSyncDataPeriod(newPeriod))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_DATA, newPeriod)
            verify(workManagerController, times(1)).cancelUniqueWork(Constants.DATA)
            verify(workManagerController, times(1)).enqueuePeriodicWork(expectedWorkerItem)
        }

    @Test
    fun shouldUpdateSyncMetadataPeriod() =
        runTest {
            val newPeriod = 13
            val expectedWorkerItem =
                WorkerItem(
                    Constants.META,
                    WorkerType.METADATA,
                    newPeriod.toLong(),
                    null,
                    null,
                    ExistingPeriodicWorkPolicy.REPLACE,
                )
            launchSync(LaunchSync.SyncAction.UpdateSyncMetadataPeriod(newPeriod))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_META, newPeriod)
            verify(workManagerController, times(1)).cancelUniqueWork(Constants.META)
            verify(workManagerController, times(1)).enqueuePeriodicWork(expectedWorkerItem)
        }

    @Test
    fun shouldCancelDataSyncIfSwitchToManual() =
        runTest {
            launchSync(LaunchSync.SyncAction.UpdateSyncDataPeriod(0))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_DATA, 0)
            verify(workManagerController, times(1)).cancelUniqueWork(Constants.DATA)
        }

    @Test
    fun shouldCancelMetadataSyncIfSwitchToManual() =
        runTest {
            launchSync(LaunchSync.SyncAction.UpdateSyncMetadataPeriod(0))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_META, 0)
            verify(workManagerController, times(1)).cancelUniqueWork(Constants.META)
        }

    @Test
    fun shouldUpdateProgressStatus() =
        runTest {
            val startedMetadataWorkInfo =
                mock<WorkInfo> {
                    on { state } doReturn WorkInfo.State.RUNNING
                }
            val startedDataWorkInfo =
                mock<WorkInfo> {
                    on { state } doReturn WorkInfo.State.RUNNING
                }
            val finishedMetadataWorkInfo =
                mock<WorkInfo> {
                    on { state } doReturn WorkInfo.State.SUCCEEDED
                }
            val finishedDataWorkInfo =
                mock<WorkInfo> {
                    on { state } doReturn WorkInfo.State.SUCCEEDED
                }
            launchSync.syncWorkInfo.test {
                mockedMetadataWorkInfo.postValue(listOf(startedMetadataWorkInfo))
                assertState(awaitItem(), LaunchSync.SyncStatus.InProgress, LaunchSync.SyncStatus.None)
                mockedDataWorkInfo.postValue(listOf(startedDataWorkInfo))
                with(awaitItem()) {
                    assertState(
                        this,
                        LaunchSync.SyncStatus.InProgress,
                        LaunchSync.SyncStatus.InProgress,
                    )
                    assertFalse(this.hasSyncFinished(metadataWasRunning = true, dataWasRunning = false))
                }

                mockedMetadataWorkInfo.postValue(listOf(finishedMetadataWorkInfo))
                with(awaitItem()) {
                    assertState(
                        this,
                        LaunchSync.SyncStatus.Finished,
                        LaunchSync.SyncStatus.InProgress,
                    )
                    assertTrue(this.hasSyncFinished(metadataWasRunning = true, dataWasRunning = true))
                }
                mockedDataWorkInfo.postValue(listOf(finishedDataWorkInfo))
                assertState(awaitItem(), LaunchSync.SyncStatus.Finished, LaunchSync.SyncStatus.Finished)
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun assertState(
        syncStatusProgress: LaunchSync.SyncStatusProgress,
        metadataSyncProgress: LaunchSync.SyncStatus,
        dataSyncProgress: LaunchSync.SyncStatus,
    ) {
        assertEquals(
            metadataSyncProgress,
            syncStatusProgress.metadataSyncProgress,
        )
        assertEquals(
            dataSyncProgress,
            syncStatusProgress.dataSyncProgress,
        )
    }
}
