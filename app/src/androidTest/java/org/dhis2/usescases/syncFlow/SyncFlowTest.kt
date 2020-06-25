package org.dhis2.usescases.syncFlow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.searchTeiRobot
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.TeiDashboardTest
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.Ignore
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
        /*
        * launch in search
        * search tei
        * change event
        * click on sync
        * check dialog (person to update)
        * click on sync
        * */

        val teiName = "Lynn"
        val teiLastName = "Dunn"

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
            Thread.sleep(4000)
            clickOnSyncTei(teiName, teiLastName) //?
            clickOnSyncButton()
        }

    }

}