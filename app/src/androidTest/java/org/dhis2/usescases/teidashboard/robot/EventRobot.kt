package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.DashboardProgramViewHolder

fun eventRobot(eventRobot: EventRobot.() -> Unit) {
    EventRobot().apply {
        eventRobot()
    }
}

class EventRobot: BaseRobot() {

    fun scrollToBottomFormulary(){
        onView(withId(R.id.formRecycler)).perform(scrollToBottomRecyclerView())
    }

    fun clickOnFormFabButton(){
        onView(withId(R.id.actionButton)).perform(click())
    }

    fun clickOnFinish(){
        onView(withId(R.id.finish)).perform(click())
    }

    fun clickOnFinishAndComplete(){
        onView(withId(R.id.complete)).perform(click())
    }

    fun fillRadioButtonForm(numberFields: Int) {
        var formLength = 0

        while (formLength < numberFields) {
            onView(withId(R.id.formRecycler))
                .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(formLength, clickChildViewWithId(R.id.yes)))
            formLength++
        }
    }

    fun clickOnChangeDate() {
        onView(withText("CHANGE DATE")).perform(click())
    }

    fun clickOnEditDate() {
        onView(withId(R.id.date)).perform(click())
    }

    fun acceptUpdateEventDate() {
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun clickOnUpdate() {
        onView(withId(R.id.action_button)).perform(click())
    }
}
