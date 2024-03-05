package org.dhis2.usescases.programevent

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.dhis2.AppTest.Companion.DB_TO_IMPORT
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.event.eventRegistrationRobot
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
    fun shouldCreateNewEventAndCompleteIt() {
        val eventOrgUnit = "Ngelehun CHC"
        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            clickOnAddEvent()
        }
        eventRegistrationRobot {
            clickNextButton()
        }
        eventRobot {
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
        }
        programEventsRobot(composeTestRule) {
            checkEventWasCreatedAndClosed(eventOrgUnit)
        }
    }

    @Test
    fun shouldOpenExistingEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            waitToDebounce(400)
            clickOnEvent(eventDate)
        }

        eventRobot {
            checkDetails(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldCompleteAnEventAndReopenIt() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            waitToDebounce(400)
            clickOnEvent(eventDate)
        }

        eventRobot {
            clickOnFormFabButton()
            clickOnCompleteButton(composeTestRule)
            waitToDebounce(400)
        }

        programEventsRobot(composeTestRule) {
            checkEventIsComplete(eventDate)
            clickOnEvent(eventDate)
        }

        eventRobot {
            clickOnDetails()
            clickOnReopen()
            pressBack()
        }

        programEventsRobot(composeTestRule) {
            waitToDebounce(800)
            checkEventIsOpen(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldOpenDetailsOfExistingEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            waitToDebounce(400)
            clickOnEvent(eventDate)
        }
        eventRobot {
            clickOnDetails()
            checkEventDetails(eventDate, eventOrgUnit, composeTestRule)
        }
    }

    @Test
    fun shouldDeleteEvent() {
        val eventDate = "15/3/2020"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            waitToDebounce(400)
            clickOnEvent(eventDate)
        }
        eventRobot {
            openMenuMoreOptions()
            clickOnDelete()
            clickOnDeleteDialog()
        }
        programEventsRobot(composeTestRule) {
            checkEventWasDeleted(eventDate, eventOrgUnit)
        }
        rule.getScenario().onActivity {
            context.applicationContext.deleteDatabase(DB_TO_IMPORT)
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
