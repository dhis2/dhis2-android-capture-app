package org.dhis2.usescases.programevent.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not

fun programEventsRobot(programEventsRobot: ProgramEventsRobot.() -> Unit) {
    ProgramEventsRobot().apply {
        programEventsRobot()
    }
}

class ProgramEventsRobot : BaseRobot() {

    fun clickOnEvent(eventDate: String) {
        onView(withId(R.id.recycler)).perform(
            RecyclerViewActions.scrollTo<EventViewHolder>(
                hasDescendant(withText(eventDate)),
            ),
            actionOnItem<EventViewHolder>(
                hasDescendant(withText(eventDate)),
                click(),
            )
        )
    }

    fun clickOnAddEvent() {
        onView(withId(R.id.addEventButton)).perform(click())
    }

    fun clickOnMap() {
        onView(withId(R.id.navigation_map_view)).perform(click())
    }

    fun checkEventWasCreatedAndClosed(eventName: String, position: Int) {
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

    fun checkEventIsComplete(eventDate: String, eventOrgUnit: String) {
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
                                            equalTo(R.drawable.ic_event_status_complete),
                                            equalTo(R.drawable.ic_event_status_complete_read)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
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