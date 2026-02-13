package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage
import org.dhis2.commons.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.commons.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.hamcrest.CoreMatchers.allOf

fun eventRobot(
    composeTestRule: ComposeTestRule,
    eventRobot: EventRobot.() -> Unit
) {
    EventRobot(composeTestRule).apply {
        eventRobot()
    }
}

class EventRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnFormFabButton() {
        waitForView(withId(R.id.actionButton)).perform(click())
    }

    fun clickOnNotNow() {
        composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).performClick()
    }

    fun clickOnCompleteButton() {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnReopen() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("REOPEN_BUTTON"))
        composeTestRule.onNodeWithTag("REOPEN_BUTTON", useUnmergedTree = true).performClick()
    }

    fun openMenuMoreOptions() {
        waitForView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val deleteLabel = getString(R.string.delete)
            composeTestRule.onNodeWithText(deleteLabel).performClick()
            composeTestRule.waitForIdle()
        }
    }

    fun clickOnDeleteDialog() {
        waitForView(withId(R.id.possitive)).perform(click())
    }

    fun checkEventDetails(eventDate: String, eventOrgUnit: String) {
        waitForView((withId(R.id.completion)))
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(100)))
        val formattedDate = formatStoredDateToUI(eventDate)
        composeTestRule.onNodeWithText(formattedDate).assertIsDisplayed()
        composeTestRule.onNodeWithText(eventOrgUnit).assertIsDisplayed()
    }

    fun checkEventCaptureActivityIsLaunched() {
        Intents.intended(allOf(IntentMatchers.hasComponent(EventCaptureActivity::class.java.name)))
    }

    fun checkEventIsOpen() {
        composeTestRule.onNodeWithTag("REOPEN_BUTTON").assertIsNotDisplayed()
    }

    private fun formatStoredDateToUI(dateValue: String): String {
        val components = dateValue.split("/")

        val year = components[2]
        val month = if (components[1].length == 1) {
            "0${components[1]}"
        } else {
            components[1]
        }
        val day = if (components[0].length == 1) {
            "0${components[0]}"
        } else {
            components[0]
        }

        return "$day/$month/$year"
    }
}
