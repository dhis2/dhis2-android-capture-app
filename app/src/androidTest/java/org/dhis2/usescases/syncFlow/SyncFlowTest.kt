package org.dhis2.usescases.syncFlow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.common.rules.DataBindingIdlingResourceRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.searchTeiRobot
import org.dhis2.usescases.syncFlow.robot.dataSetRobot
import org.dhis2.usescases.syncFlow.robot.eventWithoutRegistrationRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.TeiDashboardRobot.Companion.OPEN_EVENT_STATUS
import org.dhis2.usescases.teidashboard.robot.TeiDashboardRobot.Companion.OVERDUE_EVENT_STATUS
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
    val ruleEventWithoutRegistration = ActivityTestRule(ProgramEventDetailActivity::class.java, false, false)

    override fun setUp() {
        super.setUp()
        setupMockServer()
        turnOnConnectivityAfterLogin()
    }

    @Test
    fun shouldSuccessfullySyncAChangedTEI() {
        val teiName =  "Scott"
        val teiLastName =  "Kelley"

        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_OK)

        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(TB_VISIT)
            clickOnEventWith(TB_VISIT, OVERDUE_EVENT_STATUS)
            waitToDebounce(600)
        }

        eventRobot {
            clickOnUpdate()
            pressBack()
            waitToDebounce(600)
        }

        syncFlowRobot {
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
            Thread.sleep(2000)
            checkSyncWasSuccessfully()
        }
    }

    @Test
    fun shouldShowErrorWhenTEISyncFails() {
        val teiName = "Lars"
        val teiLastName = "Overland"

        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_ERROR)

        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnGroupEventByName(LAB_MONITORING)
            clickOnEventWith(LAB_MONITORING, OPEN_EVENT_STATUS)
            waitToDebounce(600)
        }

        eventRobot {
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnFinishAndComplete()
            waitToDebounce(600)
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

    @Test
    @Ignore("check mockserver and calls")
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
        const val LAB_MONITORING = "Lab monitoring"
        const val TB_VISIT = "TB visit"

        const val SYNC_TEI_PATH = "/api/trackedEntityInstances?.*"
        const val API_SYNC_TEI_OK = "mocks/syncFlow/teiSync.json"
        const val API_SYNC_TEI_ERROR = "mocks/syncFlow/teiSyncError.json"

        const val SYNC_EVENT_PATH = "/api/events?strategy=SYNC?.*"
        const val API_SYNC_EVENT_OK = "mocks/syncFlow/teiSync.json"

        const val SYNC_DATASET_PATH = "/api/completeDataSetRegistrations?.*"
        const val API_SYNC_DATASET_OK = "mocks/syncFlow/datasetSync.json"
    }
}