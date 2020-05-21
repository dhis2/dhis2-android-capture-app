package org.dhis2.usescases.teidashboard.robot

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.hamcrest.Matchers.allOf

fun eventRobot(eventRobot: EventRobot.() -> Unit) {
    EventRobot().apply {
        eventRobot()
    }
}

class EventRobot: BaseRobot() {

    fun scrollToBottomFormulary(){
        onView(withId(R.id.formRecycler)).perform(scrollToBottomRecyclerView())
    }

    fun clickOnFormularyFabButton(){
        onView(withId(R.id.actionButton)).perform(click())
    }

    fun editFormTypeComment() {
        onView(withId(R.id.formRecycler))
                .perform(actionOnItem<RecyclerView.ViewHolder>(hasDescendant(allOf(withId(R.id.input_editText), withText("Visit comment (optional)"))), TypeTextAction("comment test")))
                //.perform(actionOnItemAtPosition<>(3, TypeTextAction("comment test")))
        closeKeyboard()
    }

    fun clickOnFinish(){
        onView(withId(R.id.finish)).perform(click())
    }
}
