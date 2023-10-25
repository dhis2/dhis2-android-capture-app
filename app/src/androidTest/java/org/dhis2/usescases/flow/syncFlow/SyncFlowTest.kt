package org.dhis2.usescases.flow.syncFlow

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var workInfoStatusLiveData: MutableLiveData<List<WorkInfo>>

    override fun setUp() {
        super.setUp()
        workInfoStatusLiveData =
            ApplicationProvider.getApplicationContext<AppTest>().mutableWorkInfoStatuses
    }

    @Test
    fun shouldSuccessfullySyncAChangedTEI() {
        val teiName = "Scott"
        val teiLastName = "Kelley"

        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)
        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(teiName, 0)
            typeAttributeAtPosition(teiLastName, 1)
            clickOnSearch()
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(TB_VISIT)
            clickOnEventWith(TB_VISIT_EVENT_DATE, ORG_UNIT)
        }

        eventRobot {
            clickOnUpdate()
        }

        teiDashboardRobot {
            composeTestRule.onNodeWithText("Sync").performClick()
        }
        syncFlowRobot {
            waitToDebounce(500)
            clickOnSyncButton(composeTestRule)
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.SUCCEEDED)))
            checkSyncWasSuccessfully(composeTestRule)
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldShowErrorWhenTEISyncFails() {
        val teiName = "Lars"
        val teiLastName = "Overland"

        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(teiName, 0)
            typeAttributeAtPosition(teiLastName, 1)
            clickOnSearch()
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(LAB_MONITORING)
            clickOnEventWith(LAB_MONITORING_EVENT_DATE, ORG_UNIT)
        }

        eventRobot {
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
        }

        teiDashboardRobot {
            composeTestRule.onNodeWithText("Sync").performClick()
        }

        syncFlowRobot {
            waitToDebounce(500)
            clickOnSyncButton(composeTestRule)
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.FAILED)))
            checkSyncFailed(composeTestRule)
        }
        cleanLocalDatabase()
    }

    @Ignore("Indeterminate (flaky)")
    @Test
    fun shouldSuccessfullySyncSavedEvent() {
        prepareMalariaEventIntentAndLaunchActivity(ruleEventWithoutRegistration)

        eventWithoutRegistrationRobot {
            clickOnEventAtPosition(0)
        }

        eventRobot {
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
        }

        syncFlowRobot {
            clickOnEventToSync(0)
            clickOnSyncButton(composeTestRule)
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.SUCCEEDED)))
            checkSyncWasSuccessfully(composeTestRule)
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
            clickOnCompleteButton(composeTestRule)
        }

        syncFlowRobot {
            clickOnEventToSync(1)
            clickOnSyncButton(composeTestRule)
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.FAILED)))
            checkSyncFailed(composeTestRule)
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldSuccessfullySyncSavedDataSet() {
        prepareFacilityDataSetIntentAndLaunchActivity(ruleDataSet)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
        }

        dataSetTableRobot(composeTestRule) {
            typeOnCell("bjDvmb4bfuf", 0, 0)
            clickOnEditValue()
            typeInput("1")
            clickOnAccept()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }

        syncFlowRobot {
            clickOnDataSetToSync(0)
            clickOnSyncButton(composeTestRule)
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.SUCCEEDED)))
            checkSyncWasSuccessfully(composeTestRule) //sync failed
        }
        cleanLocalDatabase()
    }

    @Test
    fun shouldShowErrorWhenSyncDataSetFails() {
        prepareFacilityDataSetIntentAndLaunchActivity(ruleDataSet)

        dataSetRobot {
            clickOnDataSetAtPosition(1)
        }

        dataSetTableRobot(composeTestRule) {
            typeOnCell("bjDvmb4bfuf", 0, 0)
            clickOnEditValue()
            typeInput("1")
            clickOnAccept()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            pressBack()
            composeTestRule.waitForIdle()
            clickOnSaveButton()
            waitToDebounce(500)
            clickOnNegativeButton()
        }

        syncFlowRobot {
            clickOnDataSetToSync(1)
            clickOnSyncButton(composeTestRule)
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.RUNNING)))
            workInfoStatusLiveData.postValue(arrayListOf(mockedGranularWorkInfo(WorkInfo.State.FAILED)))
            checkSyncFailed(composeTestRule)
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
            0,
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