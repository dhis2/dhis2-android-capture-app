package org.dhis2.usescases.flow.syncFlow

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.work.Data
import androidx.work.WorkInfo
import org.dhis2.AppTest
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.dataSetTableRobot
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
import org.dhis2.usescases.flow.syncFlow.robot.eventWithoutRegistrationRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import syncFlowRobot
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SyncFlowTest : BaseTest() {

    @get:Rule
    val ruleDataSet = ActivityTestRule(DataSetDetailActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @get:Rule
    val ruleEventWithoutRegistration =
        ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    private lateinit var workInfoStatusLiveData: MutableLiveData<List<WorkInfo>>

    override fun setUp() {
        super.setUp()
        workInfoStatusLiveData =
            ApplicationProvider.getApplicationContext<AppTest>().mutableWorkInfoStatuses
    }

    @Test
    @Ignore
    fun shouldSuccessfullySyncAChangedTEI() {
        val teiName = "Scott"
        val teiLastName = "Kelley"

        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(TB_VISIT)
            clickOnEventWith(TB_VISIT_EVENT_DATE, ORG_UNIT)
        }

        eventRobot {
            clickOnUpdate()
        }

        syncFlowRobot {
            pressBack()
            waitToDebounce(500)
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.SUCCEEDED)))
            checkSyncWasSuccessfully()
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldShowErrorWhenTEISyncFails() {
        val teiName = "Lars"
        val teiLastName = "Overland"

        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(LAB_MONITORING)
            clickOnEventWith(LAB_MONITORING_EVENT_DATE, ORG_UNIT)
        }

        eventRobot {
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnFinishAndComplete()
        }

        teiDashboardRobot {
            pressBack()
        }

        syncFlowRobot {
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.FAILED)))
            checkSyncFailed()
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldSuccessfullySyncSavedEvent() {
        prepareMalariaEventIntentAndLaunchActivity(ruleEventWithoutRegistration)

        eventWithoutRegistrationRobot {
            clickOnEventAtPosition(0)
        }

        eventRobot {
            clickOnFormFabButton()
            clickOnFinishAndComplete()
        }

        syncFlowRobot {
            clickOnEventToSync(0)
            clickOnSyncButton()
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.SUCCEEDED)))
            checkSyncWasSuccessfully()
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldShowErrorWhenSyncEventFails() {
        prepareMalariaEventIntentAndLaunchActivity(ruleEventWithoutRegistration)

        eventWithoutRegistrationRobot {
            clickOnEventAtPosition(1)
        }

        eventRobot {
            clickOnFormFabButton()
            clickOnFinishAndComplete()
        }

        syncFlowRobot {
            clickOnEventToSync(1)
            clickOnSyncButton()
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.FAILED)))
            checkSyncFailed()
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldSuccessfullySyncSavedDataSet() {
        prepareFacilityDataSetIntentAndLaunchActivity(ruleDataSet)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot {
            typeOnEditTextCell("1", 0, 0)
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }

        syncFlowRobot {
            clickOnDataSetToSync(0)
            clickOnSyncButton()
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.SUCCEEDED)))
            checkSyncWasSuccessfully() //sync failed
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldShowErrorWhenSyncDataSetFails() {
        prepareFacilityDataSetIntentAndLaunchActivity(ruleDataSet)

        dataSetRobot {
            clickOnDataSetAtPosition(1)
        }

        dataSetTableRobot {
            typeOnEditTextCell("1", 0, 0)
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }

        syncFlowRobot {
            clickOnDataSetToSync(1)
            clickOnSyncButton()
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.FAILED)))
            checkSyncFailed()
        }
        cleanLocalDatabase()
    }

    private fun mockedGranularWorkInfo(state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            UUID.randomUUID(),
            state,
            Data.EMPTY,
            arrayListOf("GRANULAR"),
            Data.EMPTY,
            0
        )
    }

    companion object {
        const val ORG_UNIT = "Ngelehun CHC"
        const val TB_VISIT = "TB visit"
        const val TB_VISIT_EVENT_DATE = "3/7/2019"
        const val LAB_MONITORING = "Lab monitoring"
        const val LAB_MONITORING_EVENT_DATE = "28/6/2020"
    }
}