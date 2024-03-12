package org.dhis2.usescases.programevent.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not

fun programEventsRobot(
    composeTestRule: ComposeContentTestRule,
    programEventsRobot: ProgramEventsRobot.() -> Unit
) {
    ProgramEventsRobot(composeTestRule).apply {
        programEventsRobot()
    }
}

class ProgramEventsRobot(val composeTestRule: ComposeContentTestRule) : BaseRobot() {

    fun clickOnEvent(eventDate: String) {
        composeTestRule.onNodeWithText(eventDate).performClick()
    }

    fun clickOnAddEvent() {
        onView(withId(R.id.addEventButton)).perform(click())
    }

    fun clickOnMap() {
        onView(withId(R.id.navigation_map_view)).perform(click())
    }

    fun checkEventWasCreatedAndClosed(eventName: String) {
        waitForView(
            allOf(
                withId(R.id.recycler),
                hasDescendant(withText(eventName)),
                hasDescendant(
                    withTagValue(
                        anyOf(
                            equalTo(R.drawable.ic_event_status_complete),
                            equalTo(R.drawable.ic_event_status_complete_read)
                        )
                    )
                )
            )
        ).check(matches(isDisplayed()))
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkEventIsComplete(eventDate: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(eventDate))
        composeTestRule.onNodeWithText(eventDate).assertIsDisplayed()
        composeTestRule.onNodeWithText("Event completed").assertIsDisplayed()
    }

    fun checkEventIsOpen(eventDate: String, eventOrgUnit: String) {
        onView(withId(R.id.recycler))
            .check(
                matches(
                    allOf(
                        hasItem(
                            allOf(
                                hasDescendant(withText(eventDate)),
                                hasDescendant(withText(eventOrgUnit)),
                                hasDescendant(
                                    withTagValue(
                                        anyOf(
                                            equalTo(R.drawable.ic_event_status_open),
                                            equalTo(R.drawable.ic_event_status_open_read)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    fun checkEventWasDeleted(eventDate: String, eventOrgUnit: String) {
        onView(withId(R.id.recycler))
            .check(
                matches(
                    not(
                        hasItem(
                            allOf(
                                hasDescendant(withText(eventDate)),
                                hasDescendant(withText(eventOrgUnit))
                            )
                        )
                    )
                )
            )
    }

    fun checkMapIsDisplayed() {
        onView(withId(R.id.mapView)).check(matches(isDisplayed()))
        onView(withId(R.id.map_carousel)).check(matches(isDisplayed()))
    }

}