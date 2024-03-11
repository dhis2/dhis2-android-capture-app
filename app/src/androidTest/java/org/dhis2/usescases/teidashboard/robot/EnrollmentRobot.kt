package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.form.ui.FormViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.teiDashboard.teiProgramList.ui.PROGRAM_TO_ENROLL
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString

fun enrollmentRobot(enrollmentRobot: EnrollmentRobot.() -> Unit) {
    EnrollmentRobot().apply {
        enrollmentRobot()
    }
}

class EnrollmentRobot : BaseRobot() {

    fun clickOnAProgramForEnrollment(composeTestRule: ComposeTestRule, program: String) {
        composeTestRule.onNodeWithTag(PROGRAM_TO_ENROLL.format(program))
            .performClick()
    }

    fun clickOnAcceptInDatePicker() {
        waitForView(withId(R.id.acceptBtn)).perform(click())
    }

    fun clickOnSaveEnrollment() {
        onView(withId(R.id.save)).perform(click())
    }

    fun clickOnPersonAttributes(attribute: String) {
        onView(withId(R.id.recyclerView))
            .perform(actionOnItem<FormViewHolder>(
                hasDescendant(withText(containsString(attribute))), clickChildViewWithId(R.id.section_details)))
    }

    fun clickOnPersonAttributesUsingButton(attribute: String){
        onView(withId(R.id.recyclerView))
            .perform(actionOnItem<FormViewHolder>(
                hasDescendant(withText(containsString(attribute))), clickChildViewWithId(R.id.sectionButton)
            ))
    }

    fun typeOnRequiredTextField(text: String, position: Int) {
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItemAtPosition<FormViewHolder>(
                    position, typeChildViewWithId(text, R.id.input_editText))
            )
    }

    fun scrollToBottomProgramForm() {
        onView(withId(R.id.recyclerView)).perform(scrollToBottomRecyclerView())
    }

    fun clickOnCalendarItem() {
        onView(withId(R.id.recyclerView))
            .perform(actionOnItem<DashboardProgramViewHolder>(
                hasDescendant(withText(containsString(DATE_OF_BIRTH))), clickChildViewWithId(R.id.inputEditText)))
    }

    fun checkActiveAndPastEnrollmentDetails(enrollmentListUIModel: EnrollmentListUIModel) {
        checkHeaderAndProgramDetails(enrollmentListUIModel, ACTIVE_PROGRAMS, 1, 2, enrollmentListUIModel.currentEnrollmentDate)
        checkHeaderAndProgramDetails(enrollmentListUIModel, PAST_PROGRAMS, 3, 4, enrollmentListUIModel.pastEnrollmentDate)
    }

    private fun checkHeaderAndProgramDetails(enrollmentListUIModel: EnrollmentListUIModel, programStatus: String, headerPosition: Int, programPosition: Int, enrollmentDay: String) {
        onView(withId(R.id.recycler)).check(matches(atPosition(headerPosition, withText(programStatus))))
        onView(withId(R.id.recycler)).check(matches(allOf(atPosition(programPosition, allOf(
            hasDescendant(withText(enrollmentListUIModel.program)),
            hasDescendant(withText(enrollmentListUIModel.orgUnit)),
            hasDescendant(withText(enrollmentDay))
        )))))
    }

    fun clickOnEnrolledProgram(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<DashboardProgramViewHolder>(position, click())
            )
    }

    fun clickOnInpuAge(label: String) {
        onView(withId(R.id.recyclerView))
            .perform(actionOnItem<FormViewHolder>(
                hasDescendant(withText(label)), clickChildViewWithId(R.id.date_picker)))
    }

    fun clickOnInputDate(label: String) {
        onView(withId(R.id.recyclerView))
            .perform(actionOnItem<FormViewHolder>(
                hasDescendant(withText(label)), clickChildViewWithId(R.id.inputEditText)))
    }

    fun clickOnDatePicker() {
        onView(allOf(withId(R.id.date_picker), withHint("Date"))).perform(click())
    }

    fun selectSpecificDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withId(R.id.datePicker)).perform(
            PickerActions.setDate(
                year,
                monthOfYear,
                dayOfMonth
            )
        )
    }
    companion object {
        const val ACTIVE_PROGRAMS = "Active programs"
        const val PAST_PROGRAMS = "Past programs"
        const val ENROLL = "ENROLL"
        const val DATE_OF_BIRTH = "Date of birth"
    }
}