package org.dhis2.usescases.syncFlow.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.hamcrest.Matchers.allOf

fun eventWithoutRegistrationRobot(eventWithoutRegistrationRobot: EventWithoutRegistrationRobot.() -> Unit) {
    EventWithoutRegistrationRobot().apply {
        eventWithoutRegistrationRobot()
    }
}

class EventWithoutRegistrationRobot : BaseRobot() {

    fun clickOnEvent() {
        onView(withId(R.id.recycler)).perform(
            scrollTo<ProgramEventDetailViewHolder>(
                allOf(hasDescendant(withText("teiName")), hasDescendant(withText("teiLastName")))
            ),
            actionOnItem<ProgramEventDetailViewHolder>(
                allOf(hasDescendant(withText("teiName")), hasDescendant(withText("teiLastName"))), clickChildViewWithId(R.id.status_icon)
            )
        )
    }

    fun clickOnEventAtPosition(position: Int) {
        onView(withId(R.id.recycler))
            .perform(actionOnItemAtPosition<ProgramEventDetailViewHolder>(position, clickChildViewWithId(R.id.status_icon)))
    }

    fun clickOnSaveFab() {
        onView(withId(R.id.action_button)).perform(click())
    }
}