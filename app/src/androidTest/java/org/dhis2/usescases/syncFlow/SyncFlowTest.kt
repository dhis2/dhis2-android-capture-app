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
        val teiName = "Lynn"
        val teiLastName = "Dunn"

        mockWebServerRobot.addResponse(GET, API_SYSTEM_INFO_PATH, LoginTest.API_SYSTEM_INFO_RESPONSE_OK)
        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_OK) // check uid tei test === uid test payload

        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(TeiDashboardTest.LAB_MONITORING)
            pressBack()
        }

        syncFlowRobot {
            Thread.sleep(8000)
            clickOnSyncTei(teiName, teiLastName)
            clickOnSyncButton()
        }

        syncFlowRobot {
            Thread.sleep(4000)
           // checkSyncWasSuccessfully() //sync failed
        }

    }

    @Test
    fun shouldShowErrorWhenTEISyncFails() {
        val teiName = "Lars"
        val teiLastName = "Overland"

        mockWebServerRobot.addResponse(POST, SYNC_TEI_PATH, API_SYNC_TEI_OK)

        setupCredentials()
        prepareTBProgrammeIntentAndLaunchActivity(ruleSearch)

        searchTeiRobot {
            closeSearchForm()
            Thread.sleep(4000)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnFab()
            clickOnScheduleNew()
            clickOnFirstReferralEvent()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
           // checkEventWasCreated(TeiDashboardTest.LAB_MONITORING)
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

        const val SYNC_TEI_INFO_PATH = "/api/system/info?fields=serverDate,dateFormat,version,contextPath,systemName"
    }
}


//{"contextPath":"https://play.dhis2.org/android-current","userAgent":"com.dhis2.debug/1.1.2/2.2.0/Android_27","calendar":"iso8601","dateFormat":"yyyy-mm-dd","serverDate":"2020-06-25T16:23:50.756","lastAnalyticsTableSuccess":"2020-03-19T07:42:21.595","intervalSinceLastAnalyticsTableSuccess":"2359 h, 41 m, 29 s","lastAnalyticsTableRuntime":"5 m, 4 s","lastSystemMonitoringSuccess":"2019-03-26T17:07:15.418","version":"2.34.0","revision":"b9e5b0f","buildTime":"2020-04-24T08:58:43.000","jasperReportsVersion":"6.3.1","environmentVariable":"DHIS2_HOME","databaseInfo":{"spatialSupport":true},"encryption":false,"emailConfigured":false,"redisEnabled":false,"systemId":"eed3d451-4ff5-4193-b951-ffcc68954299","systemName":"DHIS 2 Demo - Sierra Leone","clusterHostname":"","isMetadataVersionEnabled":true,"metadataSyncEnabled":false}