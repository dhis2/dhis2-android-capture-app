package org.dhis2.usescases.programevent

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Rule
import org.junit.Test

class ProgramEventTest : BaseTest() {

    private val antenatalCare = "lxAQ7Zs9VYR"
    private val informationCampaign = "q04UBOqq3rp"

    @get:Rule
    val rule = lazyActivityScenarioRule<ProgramEventDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @Test
    fun shouldOpenExistingEvent() {
        enableIntents()
        val eventDate = "07/04/2024"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            clickOnEvent(eventDate)
        }

        eventRobot(composeTestRule) {
            checkEventCaptureActivityIsLaunched()
            checkEventDetails(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldCompleteAnEventAndReopenIt() {
        val eventDate = "07/04/2024"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            clickOnEvent(eventDate)
        }

        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }

        programEventsRobot(composeTestRule) {
            checkEventIsComplete(eventDate)
            clickOnEvent(eventDate)
        }

        eventRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnReopen()
            checkEventIsOpen()
        }
    }

    @Test
    fun shouldDeleteEvent() {
        val eventDate = "26/11/2021"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            clickOnEvent(eventDate)
        }
        eventRobot(composeTestRule) {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }
        programEventsRobot(composeTestRule) {
            checkEventWasDeleted(eventDate)
        }
    }

    @Test
    fun shouldOpenEventAndShowMap() {
        prepareProgramAndLaunchActivity(informationCampaign)

        programEventsRobot(composeTestRule) {
            clickOnMap()
            checkMapIsDisplayed()
        }
    }

    private fun prepareProgramAndLaunchActivity(programUid: String) {
        Intent(
            ApplicationProvider.getApplicationContext(),
            ProgramEventDetailActivity::class.java,
        ).apply {
            putExtra(ProgramEventDetailActivity.EXTRA_PROGRAM_UID, programUid)
        }.also { rule.launch(it) }
    }
}
