package org.dhis2.usescases.event

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teiFlow.TeiFlowTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest: BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(EventCaptureActivity::class.java, false, false)

    @get:Rule
    val ruleTeiDashboard = ActivityTestRule(TeiDashboardMobileActivity::class.java, false, false)

    @Ignore
    @Test
    fun shouldDeleteEventWhenClickOnDeleteInsideSpecificEvent() {

        /**
         * Open and launch TEI
         * click on event
         * click on menu
         * click on Delete
         * accept dialog
         * check list of events, event was deleted
         * */

    }

    @Test
    fun shouldShowEventDetailsWhenClickOnDetailsInsideSpecificEvent() {

        /**
         * Open and launch TEI
         * click on event
         * click on menu
         * click on Details
         * check %, OU and date
         * */

        // Drew Vaughn
        // tb visit (3) 20/6/2019 ehxNi0D67uQ

        //Berhane Yusef Lab monitoring 1/6/2020  tBAguooTVqg  ur1Edk5Oe2n CHECK  Ngelehun CHC 50%
        // Zimmerman Lab monitoring 6/8/2020  tNbDeLvprys ur1Edk5Oe2n CHECK 100
        // Jason Smith Sputum smear microscopy test  20/4/2020  E9CNT1WyBaX  ur1Edk5Oe2n CHECK 0
        // Sputum smear microscopy test  2/8/2020  E9CNT1WyBaX  ur1Edk5Oe2n 100

        prepareEventDetailsIntentAndLaunchActivity(rule)

        eventRegistrationRobot {
            openMenuMoreOptions()
            Thread.sleep(7000)
            clickOnDetails()
            checkEventDetails()
        }

    }


    private fun prepareEventDetailsIntentAndLaunchActivity(rule: ActivityTestRule<EventCaptureActivity>) {
        Intent().apply {
            putExtra(PROGRAM_UID, PROGRAM_TB)
            putExtra(EVENT_UID, EVENT_DETAILS_UID)
        }.also { rule.launchActivity(it) }
    }

    companion object {
        const val EVENT_UID = "EVENT_UID"
        const val PROGRAM_UID = "PROGRAM_UID"

        const val PROGRAM_TB = "ur1Edk5Oe2n"
        const val EVENT_DETAILS_UID =  "DSKhD4VgQPQ"//"E9CNT1WyBaX" //"tNbDeLvprys" //"tBAguooTVqg"
    }

}