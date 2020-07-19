package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextCustomHolder
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.dhis2.usescases.teidashboard.entity.EnrollmentListUIModel
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

fun enrollmentRobot(enrollmentRobot: EnrollmentRobot.() -> Unit) {
    EnrollmentRobot().apply {
        enrollmentRobot()
    }
}

class EnrollmentRobot : BaseRobot() {

    fun clickOnAProgramForEnrollment(program: String) {
        onView(withId(R.id.recycler))
            .perform(
                scrollTo<SearchTEViewHolder>(hasDescendant(withText(program))),
                actionOnItem<DashboardProgramViewHolder>(hasDescendant(withText(program)), clickChildViewWithId(R.id.action_button))
            )
    }

    fun clickOnSameProgramForEnrollment(program: String) {
        onView(withId(R.id.recycler))
            .perform(
                scrollTo<SearchTEViewHolder>(allOf(hasDescendant(withText(program)), hasDescendant(
                    withText("ENROLL")))),
                actionOnItem<DashboardProgramViewHolder>(allOf(hasDescendant(withText(program)), hasDescendant(
                    withText("ENROLL"))), clickChildViewWithId(R.id.action_button))
            )
    }

    fun clickOnAcceptEnrollmentDate() {
        onView(withId(R.id.acceptButton)).perform(click())
    }

    fun clickOnSaveEnrollment() {
        onView(withId(R.id.save)).perform(click())
    }

    fun clickOnPersonAttributes(attribute: String) {
        onView(withId(R.id.fieldRecycler))
            .perform(actionOnItem<EditTextCustomHolder>(
                hasDescendant(withText(containsString(attribute))), click()))
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

    fun clickOnCalendarItem() {
        onView(withId(R.id.fieldRecycler))
            .perform(actionOnItem<DashboardProgramViewHolder>(
                hasDescendant(withText("Date of birth*")), clickChildViewWithId(R.id.inputEditText)))
    }

    fun checkActiveAndPastEnrollmentDetails(enrollmentListUIModel: EnrollmentListUIModel) {
        onView(withId(R.id.recycler)).check(matches(hasItem(allOf(
            hasDescendant(withText(enrollmentListUIModel.program)),
            hasDescendant(withText(enrollmentListUIModel.orgUnit)))
        )))

        onView(withId(R.id.recycler)).check(matches(hasItem(allOf(
            hasDescendant(withText(enrollmentListUIModel.program)),
            hasDescendant(withText(enrollmentListUIModel.orgUnit)),
            hasDescendant(withText(enrollmentListUIModel.pastEnrollmentDate))
        ))))

        //onView(withId(R.id.recycler)).check(matches(atPosition(2, allOf(hasDescendant(withText(R.string.active_programs))))))
        /*onView(withId(R.id.recycler)).check(matches(allOf(atPosition(2, allOf(
            hasDescendant(withText(enrollmentListUIModel.program)),
            hasDescendant(withText(enrollmentListUIModel.orgUnit))
         //   hasDescendant(withText(enrollmentListUIModel.currentEnrollmentDate))
        )))))
        onView(withId(R.id.recycler)).check(matches(atPosition(3, allOf(hasDescendant(withText(R.string.past_programs))))))
        onView(withId(R.id.recycler)).check(matches(atPosition(4, allOf(
            hasDescendant(withText(enrollmentListUIModel.program)),
            hasDescendant(withText(enrollmentListUIModel.orgUnit)),
            hasDescendant(withText(enrollmentListUIModel.pastEnrollmentDate))
        ))))*/
    }

    fun clickOnEnrolledProgram(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<DashboardProgramViewHolder>(position, click())
            )
    }


}