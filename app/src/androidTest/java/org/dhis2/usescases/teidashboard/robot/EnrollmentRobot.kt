package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextCustomHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder

fun enrollmentRobot(enrollmentRobot: EnrollmentRobot.() -> Unit) {
    EnrollmentRobot().apply {
        enrollmentRobot()
    }
}

class EnrollmentRobot : BaseRobot() {

    fun clickOnAProgramForEnrollment(position: Int) {
        onView(withId(R.id.recycler)).perform(
            actionOnItemAtPosition<DashboardProgramViewHolder>(
                position,
                clickChildViewWithId(R.id.action_button)
            )
        )
    }

    fun clickOnAcceptEnrollmentDate() {
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun clickOnSaveEnrollment() {
        onView(withId(R.id.save)).perform(click())
    }

    fun clickOnPersonAttributes(position: Int) {
        onView(withId(R.id.fieldRecycler))
            .perform(actionOnItemAtPosition<EditTextCustomHolder>(position, click()))
    }

    fun typeOnRequiredTextField(text: String, position: Int) {
        onView(withId(R.id.fieldRecycler))
            .perform(
                actionOnItemAtPosition<EditTextCustomHolder>(
                    position, typeChildViewWithId(text, R.id.input_editText))
            )
    }

    fun scrollToBottomProgramForm() {
        onView(withId(R.id.fieldRecycler)).perform(scrollToBottomRecyclerView())
    }

    fun clickOnCalendarItem(position: Int) {
        onView(withId(R.id.fieldRecycler)).perform(
            actionOnItemAtPosition<DashboardProgramViewHolder>(
                position,
                clickChildViewWithId(R.id.inputEditText)
            )
        )
    }

}