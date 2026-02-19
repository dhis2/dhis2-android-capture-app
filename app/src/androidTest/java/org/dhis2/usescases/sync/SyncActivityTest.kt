package org.dhis2.usescases.sync

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.model.SyncJobStatus
import org.dhis2.mobile.sync.model.SyncStatus
import org.dhis2.usescases.BaseTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever


@RunWith(AndroidJUnit4::class)
class SyncActivityTest : BaseTest(), KoinTest {
    private val metadataSyncJobStatuses = MutableStateFlow<List<SyncJobStatus>>(emptyList())

    @get:Rule
    val mockProvider =
        MockProviderRule.create { clazz ->
            Mockito.mock(clazz.java)
        }

    @After
    override fun teardown() {
        super.teardown()
        metadataSyncJobStatuses.tryEmit(emptyList())
    }

    @Test
    fun shouldShowMetadataErrorDialog() {

        declareMock<SyncBackgroundJobAction> {
            whenever(launchMetadataSync(any())) doAnswer {}
            whenever(observeMetadataJob()) doReturn metadataSyncJobStatuses
        }

        ActivityScenario.launch(SyncActivity::class.java).use {
            syncRobot {
                waitUntilActivityVisible<SyncActivity>()
                waitToDebounce(3000)
                metadataSyncJobStatuses.tryEmit(listOf(mockedMetadataJobStatus(SyncStatus.Running)))
                waitToDebounce(3000)
                metadataSyncJobStatuses.tryEmit(listOf(mockedMetadataJobStatus(SyncStatus.Failed)))
                waitToDebounce(3000)
                checkMetadataErrorDialogIsDisplayed()
            }
        }
    }

    @Test
    fun shouldCompleteSyncProcess() {
        enableIntents()

        declareMock<SyncBackgroundJobAction> {
            whenever(launchMetadataSync(any())) doAnswer {}
            whenever(observeMetadataJob()) doReturn metadataSyncJobStatuses
        }

        ActivityScenario.launch(SyncActivity::class.java).use {
            syncRobot {
                waitUntilActivityVisible<SyncActivity>()
                metadataSyncJobStatuses.tryEmit(listOf(mockedMetadataJobStatus(SyncStatus.Running)))
                waitToDebounce(3000)
                checkMetadataIsSyncing()
                checkDataIsWaiting()
                metadataSyncJobStatuses.tryEmit(listOf(mockedMetadataJobStatus(SyncStatus.Succeed)))
                waitToDebounce(3000)
                checkMainActivityIsLaunched()
            }
        }
    }

    private fun mockedMetadataJobStatus(status: SyncStatus) = SyncJobStatus(
        tags = listOf("METADATA_SYNC", "METADATA_SYNC_NOW"),
        status = status,
        message = null
    )
}
