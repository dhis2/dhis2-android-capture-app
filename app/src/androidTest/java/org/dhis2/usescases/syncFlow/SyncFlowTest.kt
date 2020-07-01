package org.dhis2.usescases.syncFlow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryActivity
import org.dhis2.usescases.login.LoginTest
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.searchTeiRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.TeiDashboardTest
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.hisp.dhis.android.core.mockwebserver.ResponseController.API_SYSTEM_INFO_PATH
import org.hisp.dhis.android.core.mockwebserver.ResponseController.GET
import org.hisp.dhis.android.core.mockwebserver.ResponseController.POST
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncFlowTest : BaseTest() {

    @get:Rule
    val ruleTei = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)
    @get:Rule
    val ruleDataSet = ActivityTestRule(EventSummaryActivity::class.java, false, false)
    @get:Rule
    val ruleSearch = ActivityTestRule(SearchTEActivity::class.java, false, false)

    @Test
    fun shouldSuccessfullySyncAChangedTEI() {
        val teiName =  "Scott" //"Lynn"
        val teiLastName =  "Kelley"//"Dunn"
        val tbVisit = 0

        //instead of creating a new event just edit (skip) one

        mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO_PATH, LoginTest.API_SYSTEM_INFO_RESPONSE_OK)
        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_OK)

        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            /*clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(TeiDashboardTest.LAB_MONITORING)*/
            clickOnEventWithPosition(tbVisit)
        }

        eventRobot {
            clickOnUpdate()
            pressBack()
        }

        syncFlowRobot {
            Thread.sleep(8000)
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
        }

        syncFlowRobot {
            Thread.sleep(4000)
            checkSyncWasSuccessfully() //sync failed
        }

    }

    @Test
    fun shouldShowErrorWhenTEISyncFails() {
        val teiName = "Lars"
        val teiLastName = "Overland"
        val labMonitoring = 1
        //instead of creating a new event I will just edit one

        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_ERROR)

        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnEventWithPosition(labMonitoring)
            waitToDebounce(600)
        }

        eventRobot {
            fillRadioButtonForm(4)
            clickOnFormFabButton()
            clickOnFinishAndComplete()
            waitToDebounce(600)
        }

        teiDashboardRobot {
            checkEventWasCreatedAndClosed(TeiDashboardTest.LAB_MONITORING, 1)
            pressBack()
        }

        syncFlowRobot {
            Thread.sleep(4000)
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
        }

        syncFlowRobot {
            Thread.sleep(4000)
            checkSyncFailed()
        }
    }


    companion object {
        const val SYNC_TEI_PATH = "/api/trackedEntityInstances?strategy=SYNC"
        const val API_SYNC_TEI_OK = "mocks/syncFlow/teiSync.json"
        const val API_SYNC_TEI_ERROR = "mocks/syncFlow/teiSyncError.json"
    }
}
