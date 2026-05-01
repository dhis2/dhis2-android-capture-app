package org.dhis2.usescases.event

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
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

    fun checkEventDataEntryIsOpened(completion: Int, orgUnit: String) {
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(completion)))
        composeTestRule.onNodeWithText(orgUnit).performScrollTo().assertIsDisplayed()
    }
}
