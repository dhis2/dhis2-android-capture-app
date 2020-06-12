package org.dhis2.usescases.syncFlow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryActivity
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

    @Test
    @Ignore
    fun shouldSuccessfullySyncAChangedEvent() {
        /*
        * launch tei
        * change event
        * sync
        * */

        //startTeiActivity()

        teiDashboardRobot {
            clickOnFab()
            clickOnReferral()
            clickOnFirstReferralEvent()
            clickOnReferralOption()
            clickOnReferralNextButton()
            checkEventCreatedToastIsShown()
            checkEventWasCreated(TeiDashboardTest.LAB_MONITORING)
        }

    }

}