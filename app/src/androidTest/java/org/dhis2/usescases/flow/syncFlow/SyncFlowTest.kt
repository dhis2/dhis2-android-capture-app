package org.dhis2.usescases.flow.syncFlow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.flow.syncFlow.robot.dataSetRobot
import org.dhis2.usescases.flow.syncFlow.robot.eventWithoutRegistrationRobot
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.hisp.dhis.android.core.mockwebserver.ResponseController.POST
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import syncFlowRobot

@RunWith(AndroidJUnit4::class)
class SyncFlowTest : BaseTest() {

    @get:Rule
    val ruleDataSet = ActivityTestRule(DataSetDetailActivity::class.java, false, false)

    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @get:Rule
    val ruleEventWithoutRegistration =
        ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        setupMockServer()
    }

    @Test
    @Ignore
    fun shouldSuccessfullySyncAChangedTEI() {
        val teiName = "Scott"
        val teiLastName = "Kelley"

        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_OK)

        turnOnConnectivityAfterLogin()
        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(TB_VISIT)
            clickOnEventWith(TB_VISIT_EVENT_DATE, ORG_UNIT)
        }

        eventRobot {
            clickOnUpdate()
            pressBack()
        }

        syncFlowRobot {
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
            checkSyncWasSuccessfully()
        }
    }

    @Test
    @Ignore
    fun shouldShowErrorWhenTEISyncFails() {
        val teiName = "Lars"
        val teiLastName = "Overland"

        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_ERROR)

        turnOnConnectivityAfterLogin()
        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
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
            checkSyncFailed()
        }
    }

    @Ignore
    @Test
    fun shouldSuccessfullySyncSavedEvent() {
        mockWebServerRobot.addResponse(POST, SYNC_EVENT_PATH, API_SYNC_EVENT_OK)

        setupCredentials()
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
            checkSyncWasSuccessfully()
        }
    }

    @Test
    @Ignore
    fun shouldShowErrorWhenSyncEventFails() {
        mockWebServerRobot.addResponse(POST, SYNC_EVENT_PATH, API_SYNC_EVENT_OK)

        setupCredentials()
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
            checkSyncFailed()
        }

    }

    @Test
    @Ignore("check mockserver and calls")
    fun shouldSuccessfullySyncSavedDataSet() {
        mockWebServerRobot.addResponse(POST, SYNC_DATASET_PATH, API_SYNC_DATASET_OK)
        setupCredentials()
        prepareFacilityDataSetIntentAndLaunchActivity(ruleDataSet)

        dataSetRobot {
            clickOnDataSetAtPosition(0)
            clickOnSave()
            pressBack()
        }

        syncFlowRobot {
            clickOnDataSetToSync(0)
            clickOnSyncButton()
            checkSyncWasSuccessfully() //sync failed
        }
    }

    @Test
    @Ignore
    fun shouldShowErrorWhenSyncDataSetFails() {
        mockWebServerRobot.addResponse(POST, SYNC_DATASET_PATH, API_SYNC_DATASET_OK)
        setupCredentials()
        prepareFacilityDataSetIntentAndLaunchActivity(ruleDataSet)

        dataSetRobot {
            clickOnDataSetAtPosition(1)
            clickOnSave()
            pressBack()
        }

        syncFlowRobot {
            clickOnDataSetToSync(1)
            clickOnSyncButton()
            checkSyncFailed()
        }
    }

    companion object {
        const val ORG_UNIT = "Ngelehun CHC"
        const val TB_VISIT = "TB visit"
        const val TB_VISIT_EVENT_DATE = "3/7/2019"
        const val LAB_MONITORING = "Lab monitoring"
        const val LAB_MONITORING_EVENT_DATE = "28/6/2020"

        const val SYNC_TEI_PATH = "/api/trackedEntityInstances?.*"
        const val API_SYNC_TEI_OK = "mocks/syncFlow/teiSync.json"
        const val API_SYNC_TEI_ERROR = "mocks/syncFlow/teiSyncError.json"

        const val SYNC_EVENT_PATH = "/api/events?strategy=SYNC?.*"
        const val API_SYNC_EVENT_OK = "mocks/syncFlow/teiSync.json"

        const val SYNC_DATASET_PATH = "/api/completeDataSetRegistrations?.*"
        const val API_SYNC_DATASET_OK = "mocks/syncFlow/datasetSync.json"
    }
}