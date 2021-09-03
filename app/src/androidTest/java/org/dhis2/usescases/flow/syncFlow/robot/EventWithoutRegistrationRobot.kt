package org.dhis2.usescases.flow.syncFlow.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder
import org.hamcrest.CoreMatchers.allOf

fun eventWithoutRegistrationRobot(eventWithoutRegistrationRobot: EventWithoutRegistrationRobot.() -> Unit) {
    EventWithoutRegistrationRobot().apply {
        eventWithoutRegistrationRobot()
    }
}

class EventWithoutRegistrationRobot : BaseRobot() {

    fun clickOnEvent() {
        onView(withId(R.id.recycler)).perform(
            scrollTo<EventViewHolder>(
                allOf(hasDescendant(withText("teiName")), hasDescendant(withText("teiLastName")))
            ),
            actionOnItem<EventViewHolder>(
                allOf(hasDescendant(withText("teiName")), hasDescendant(withText("teiLastName"))),
                clickChildViewWithId(R.id.status_icon)
            )
        )
    }

    fun clickOnEventAtPosition(position: Int) {
        onView(withId(R.id.recycler))
            .perform(actionOnItemAtPosition<EventViewHolder>(position, click()))
    }

    fun clickOnSaveFab() {
        onView(withId(R.id.action_button)).perform(click())
    }
}