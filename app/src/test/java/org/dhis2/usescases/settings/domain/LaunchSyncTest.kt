package org.dhis2.usescases.settings.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.model.SyncJobStatus
import org.dhis2.mobile.sync.model.SyncStatus
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
    private val preferenceProvider: PreferenceProvider = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

    private val mockedMetadataWorkInfo = MutableStateFlow<List<SyncJobStatus>>(emptyList())
    private val mockedDataWorkInfo = MutableStateFlow<List<SyncJobStatus>>(emptyList())
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(syncBackgroundJobAction.observeMetadataJob()) doReturn mockedMetadataWorkInfo
        whenever(syncBackgroundJobAction.observeDataJob()) doReturn mockedDataWorkInfo
        launchSync =
            LaunchSync(
                syncBackgroundJobAction = syncBackgroundJobAction,
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
            launchSync(LaunchSync.SyncAction.SyncMetadata)
            verify(syncBackgroundJobAction, times(1)).launchMetadataSync(0)
        }

    @Test
    fun shouldStartSyncData() =
        runTest {
            launchSync(LaunchSync.SyncAction.SyncData)
            verify(syncBackgroundJobAction, times(1)).launchDataSync(0)
        }

    @Test
    fun shouldUpdateSyncDataPeriod() =
        runTest {
            val newPeriod = 13
            launchSync(LaunchSync.SyncAction.UpdateSyncDataPeriod(newPeriod))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_DATA, newPeriod)
            verify(syncBackgroundJobAction, times(1)).launchDataSync(newPeriod.toLong())
        }

    @Test
    fun shouldUpdateSyncMetadataPeriod() =
        runTest {
            val newPeriod = 13
            launchSync(LaunchSync.SyncAction.UpdateSyncMetadataPeriod(newPeriod))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_META, newPeriod)
            verify(syncBackgroundJobAction, times(1)).launchMetadataSync(newPeriod.toLong())
        }

    @Test
    fun shouldCancelDataSyncIfSwitchToManual() =
        runTest {
            launchSync(LaunchSync.SyncAction.UpdateSyncDataPeriod(0))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_DATA, 0)
            verify(syncBackgroundJobAction, times(1)).cancelDataSync()
        }

    @Test
    fun shouldCancelMetadataSyncIfSwitchToManual() =
        runTest {
            launchSync(LaunchSync.SyncAction.UpdateSyncMetadataPeriod(0))
            verify(preferenceProvider, times(1)).setValue(Constants.TIME_META, 0)
            verify(syncBackgroundJobAction, times(1)).cancelMetadataSync()
        }

    @Test
    fun shouldUpdateProgressStatus() =
        runTest {
            val startedMetadataWorkInfo =
                mock<SyncJobStatus> {
                    on { status } doReturn SyncStatus.Running
                }
            val startedDataWorkInfo =
                mock<SyncJobStatus> {
                    on { status } doReturn SyncStatus.Running
                }
            val finishedMetadataWorkInfo =
                mock<SyncJobStatus> {
                    on { status } doReturn SyncStatus.Succeed
                }
            val finishedDataWorkInfo =
                mock<SyncJobStatus> {
                    on { status } doReturn SyncStatus.Succeed
                }
            launchSync.syncWorkInfo.test {
                awaitItem()
                awaitItem()
                mockedMetadataWorkInfo.emit(listOf(startedMetadataWorkInfo))
                assertState(
                    awaitItem(),
                    LaunchSync.SyncStatus.InProgress,
                    LaunchSync.SyncStatus.None
                )
                mockedDataWorkInfo.emit(listOf(startedDataWorkInfo))
                with(awaitItem()) {
                    assertState(
                        this,
                        LaunchSync.SyncStatus.InProgress,
                        LaunchSync.SyncStatus.InProgress,
                    )
                    assertFalse(
                        this.hasSyncFinished(
                            metadataWasRunning = true,
                            dataWasRunning = false
                        )
                    )
                }

                mockedMetadataWorkInfo.emit(listOf(finishedMetadataWorkInfo))
                with(awaitItem()) {
                    assertState(
                        this,
                        LaunchSync.SyncStatus.Finished,
                        LaunchSync.SyncStatus.InProgress,
                    )
                    assertTrue(
                        this.hasSyncFinished(
                            metadataWasRunning = true,
                            dataWasRunning = true
                        )
                    )
                }
                mockedDataWorkInfo.emit(listOf(finishedDataWorkInfo))
                assertState(
                    awaitItem(),
                    LaunchSync.SyncStatus.Finished,
                    LaunchSync.SyncStatus.Finished
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun shouldCombineMetadataProgress() =
        runTest {
            launchSync.syncWorkInfo.test {
                awaitItem()
                awaitItem()
                syncStatuses.forEach { metadataSyncStatus ->
                    syncStatuses.forEach { metadataNowSyncStatus ->
                        mockedMetadataWorkInfo.emit(
                            listOf(
                                metadataSyncStatus,
                                metadataNowSyncStatus
                            )
                        )
                        val expectedValue = when {
                            (metadataSyncStatus.status is SyncStatus.Running) or (metadataNowSyncStatus.status is SyncStatus.Running) -> LaunchSync.SyncStatus.InProgress
                            (metadataSyncStatus.status is SyncStatus.Blocked) or (metadataNowSyncStatus.status is SyncStatus.Blocked) -> LaunchSync.SyncStatus.InProgress
                            (metadataSyncStatus.status is SyncStatus.Enqueue) and (metadataNowSyncStatus.status is SyncStatus.Enqueue) -> LaunchSync.SyncStatus.None
                            (metadataSyncStatus.status is SyncStatus.Cancelled) and (metadataNowSyncStatus.status is SyncStatus.Cancelled) -> LaunchSync.SyncStatus.Cancelled
                            else -> LaunchSync.SyncStatus.Finished


                        }

                        assertState(
                            awaitItem(),
                            expectedValue,
                            LaunchSync.SyncStatus.None
                        )
                    }
                }
            }
        }

    private val syncStatuses = listOf(
        mockedMetadataSyncJobStatus(SyncStatus.Enqueue),
        mockedMetadataSyncJobStatus(SyncStatus.Running),
        mockedMetadataSyncJobStatus(SyncStatus.Succeed),
        mockedMetadataSyncJobStatus(SyncStatus.Failed),
        mockedMetadataSyncJobStatus(SyncStatus.Blocked),
        mockedMetadataSyncJobStatus(SyncStatus.Cancelled),
    )

    private fun mockedMetadataSyncJobStatus(mockedStatus: SyncStatus) = mock<SyncJobStatus> {
        on { status } doReturn mockedStatus
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
