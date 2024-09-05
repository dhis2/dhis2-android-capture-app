package org.dhis2.usescases.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage

fun eventRegistrationRobot(eventRegistrationRobot: EventRegistrationRobot.() -> Unit) {
    EventRegistrationRobot().apply {
        eventRegistrationRobot()
    }
}

class EventRegistrationRobot : BaseRobot() {

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        onView(withText(R.string.delete)).perform(click())
    }

    fun checkEventDataEntryIsOpened(completion: Int, email: String, composeTestRule: ComposeTestRule) {
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(completion)))
        composeTestRule.onNodeWithText(email).performScrollTo()
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    fun clickOnShare() {
        onView(withText(R.string.share)).perform(click())
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
