package org.dhis2.usescases.event

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.entity.EventDetailsUIModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
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
        val eventDetails = createEventDetails()

        prepareEventDetailsIntentAndLaunchActivity(rule)

        eventRegistrationRobot {
            openMenuMoreOptions()
            clickOnDetails()
            checkEventDetails(eventDetails)
        }
    }

    @Test
    fun shouldShareQRWhenClickOnShare() {

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
        const val EVENT_DETAILS_UID =  "ZdRPhMckeJk"
    }

    private fun createEventDetails() = EventDetailsUIModel(
        "Lab monitoring",
        0.75f,
        "2/8/2020",
        "Ngelehun CHC"
    )

}