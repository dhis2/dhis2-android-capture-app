package org.dhis2.usescases.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage

fun eventRegistrationRobot(
    composeTestRule: ComposeTestRule,
    eventRegistrationRobot: EventRegistrationRobot.() -> Unit
) {
    EventRegistrationRobot(composeTestRule).apply {
        eventRegistrationRobot()
    }
}

class EventRegistrationRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.delete)).performClick()
        }
    }

    fun checkEventDataEntryIsOpened(completion: Int, email: String, composeTestRule: ComposeTestRule) {
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(completion)))
        composeTestRule.onNodeWithText(email).performScrollTo()
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    fun clickOnShare() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.share)).performClick()
        }
    }

    private fun clickOnNextQR() {
        waitForView(withId(R.id.next)).perform(click())
    }

    fun clickOnAllQR(listQR: Int) {
        var qrLength = 1

        while (qrLength < listQR) {
            clickOnNextQR()
            qrLength++
        }
    }

    fun clickOnDeleteDialog() {
        onView(withId(R.id.possitive)).perform(click())
    }

    fun clickNextButton() {
        waitForView(withId(R.id.action_button)).perform(click())
    }
}
