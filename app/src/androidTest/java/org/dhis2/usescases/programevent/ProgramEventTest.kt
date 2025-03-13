package org.dhis2.usescases.programevent

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.dhis2.AppTest.Companion.DB_TO_IMPORT
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programevent.robot.programEventsRobot
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Ignore
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
    @Ignore("Flaky test, will be looked up in ANDROAPP-6476")
    fun shouldCreateNewEventAndCompleteIt() {
        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnAddEvent()
        }
        eventRobot(composeTestRule) {
            typeOnDateParameter(
                dateValue = "01012001",
            )
            composeTestRule.waitForIdle()
            clickOnFormFabButton()
            composeTestRule.waitForIdle()
            clickOnCompleteButton()
            composeTestRule.waitForIdle()
        }
        programEventsRobot(composeTestRule) {
            checkEventWasCreatedAndClosed()
        }
    }

    @Test
    fun shouldOpenExistingEvent() {
        val eventDate = "07/04/2024"
        val eventOrgUnit = "Ngelehun CHC"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnEvent(eventDate)
            composeTestRule.waitForIdle()
        }

        eventRobot(composeTestRule) {
            checkEventDetails(eventDate, eventOrgUnit)
        }
    }

    @Test
    fun shouldCompleteAnEventAndReopenIt() {
        val eventDate = "07/04/2024"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnEvent(eventDate)
            composeTestRule.waitForIdle()
        }

        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            composeTestRule.waitForIdle()
            clickOnCompleteButton()
            composeTestRule.waitForIdle()
        }

        programEventsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            checkEventIsComplete(eventDate)
            composeTestRule.waitForIdle()
            clickOnEvent(eventDate)
            composeTestRule.waitForIdle()
        }

        eventRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnReopen()
            composeTestRule.waitForIdle()
            checkEventIsOpen()
        }
    }

    @Test
    fun shouldDeleteEvent() {
        val eventDate = "07/04/2024"

        prepareProgramAndLaunchActivity(antenatalCare)

        programEventsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnEvent(eventDate)
        }
        eventRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            openMenuMoreOptions()
            composeTestRule.waitForIdle()
            clickOnDelete()
            composeTestRule.waitForIdle()
            clickOnDeleteDialog()
            composeTestRule.waitForIdle()
        }
        programEventsRobot(composeTestRule) {
            checkEventWasDeleted(eventDate)
            composeTestRule.waitForIdle()
        }

        rule.getScenario().onActivity {
            context.applicationContext.deleteDatabase(DB_TO_IMPORT)
        }
    }

    @Test
    fun shouldOpenEventAndShowMap() {
        prepareProgramAndLaunchActivity(informationCampaign)

        programEventsRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            clickOnMap()
            composeTestRule.waitForIdle()
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
