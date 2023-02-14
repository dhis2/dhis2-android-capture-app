package org.dhis2.usescases.sync

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.work.Data
import androidx.work.WorkInfo
import org.dhis2.AppTest
import org.dhis2.usescases.BaseTest
import org.dhis2.commons.Constants
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SyncActivityTest : BaseTest() {
    private lateinit var workInfoStatusLiveData: MutableLiveData<List<WorkInfo>>

    @get:Rule
    val syncRule = ActivityTestRule(SyncActivity::class.java, false, false)


    @Before
    override fun setUp() {
        super.setUp()
        workInfoStatusLiveData =
            ApplicationProvider.getApplicationContext<AppTest>().mutableWorkInfoStatuses
    }

    @Test
    fun shouldShowMetadataErrorDialog() {
        startSyncActivity()

        syncRobot {
            waitUntilActivityVisible<SyncActivity>()
            waitToDebounce(3000)
            workInfoStatusLiveData.postValue(arrayListOf(mockedMetaWorkInfo(WorkInfo.State.RUNNING)))
            waitToDebounce(3000)
            workInfoStatusLiveData.postValue(arrayListOf(mockedMetaWorkInfo(WorkInfo.State.FAILED)))
            waitToDebounce(3000)
            checkMetadataErrorDialogIsDisplayed()
        }
    }

    @Test
    fun shouldCompleteSyncProcess() {
        startSyncActivity()
        enableIntents()

        syncRobot {
            waitUntilActivityVisible<SyncActivity>()
            workInfoStatusLiveData.postValue(arrayListOf(mockedMetaWorkInfo(WorkInfo.State.RUNNING)))
            waitToDebounce(3000)
            checkMetadataIsSyncing()
            checkDataIsWaiting()
            workInfoStatusLiveData.postValue(arrayListOf(mockedMetaWorkInfo(WorkInfo.State.SUCCEEDED)))
            waitToDebounce(3000)
            checkMainActivityIsLaunched()
        }
    }

    private fun startSyncActivity() {
        syncRule.launchActivity(null)
    }

    private fun mockedMetaWorkInfo(state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            UUID.randomUUID(),
            state,
            Data.EMPTY,
            arrayListOf(Constants.META_NOW),
            Data.EMPTY,
            0
        )
    }

    private fun mockedDataWorkInfo(state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            UUID.randomUUID(),
            state,
            Data.EMPTY,
            arrayListOf(Constants.DATA_NOW),
            Data.EMPTY,
            0
        )
    }
}

