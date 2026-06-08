package org.dhis2.usescases.event

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.usescases.teidashboard.robot.eventRobot
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<EventCaptureActivity>(launchActivity = false)

    @get:Rule
    val ruleTeiDashboard =
        lazyActivityScenarioRule<TeiDashboardMobileActivity>(launchActivity = false)

    @get:Rule
    val ruleEventDetail = lazyActivityScenarioRule<EventInitialActivity>(launchActivity = false)

    @get:Rule
    val eventListRule = lazyActivityScenarioRule<ProgramEventDetailActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowEventDetailsWhenClickOnDetailsInsideSpecificEvent() {
        val completion = 100
        val orgUnit = "Ngelehun CHC"

        prepareEventDetailsIntentAndLaunchActivity(rule)

        eventRegistrationRobot(composeTestRule) {
            checkEventDataEntryIsOpened(completion, orgUnit)
        }
    }


    @Test
    fun shouldExerciseEventDataEntryFormLifecycle() {
        prepareFlowAEventEmptyMandatoryAndLaunchActivity(rule)

        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()

            checkFieldLabelIsFormName(
                formName = "Date of admission",
                displayName = "Admission Date",
            )

            checkNoLegacyUpdateActionIsPresent()
        }

        eventRegistrationRobot(composeTestRule) {
            clickSyncButton()
        }
        rule.getScenario().onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("EVENT_SYNC")
            assertNotNull("Expected SyncStatusDialog (EVENT_SYNC) to be attached", fragment)
            assertTrue("SyncStatusDialog must be added", fragment!!.isAdded)
        }

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
        composeTestRule.waitForIdle()

        eventRobot(composeTestRule) {
            clickOnFormFabButton()
            clickOnCompleteButton()
        }
        eventRegistrationRobot(composeTestRule) {
            checkSaveButtonIsDisplayed()
        }
    }

    @Test
    fun shouldShowReadOnlySignalWhenEventIsCompleted() {
        prepareFlowAEventCompletedAndLaunchActivity(rule)

        eventRegistrationRobot(composeTestRule) {
            checkFormIsReadOnly()
        }
    }
}
