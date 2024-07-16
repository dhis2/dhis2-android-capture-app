package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.form.ui.FormViewHolder
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.dhis2.usescases.teiDashboard.teiProgramList.ui.PROGRAM_TO_ENROLL
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString

fun enrollmentRobot(
    composeTestRule: ComposeTestRule,
    enrollmentRobot: EnrollmentRobot.() -> Unit,
) {
    EnrollmentRobot(composeTestRule).apply {
        enrollmentRobot()
    }
}

class EnrollmentRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnAProgramForEnrollment(composeTestRule: ComposeTestRule, program: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(PROGRAM_TO_ENROLL.format(program), useUnmergedTree = true)
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
            .perform(
                actionOnItem<FormViewHolder>(
                    hasDescendant(withText(containsString(attribute))),
                    clickChildViewWithId(R.id.section_details)
                )
            )
    }

    fun scrollToBottomProgramForm() {
        onView(withId(R.id.recyclerView)).perform(scrollToBottomRecyclerView())
    }

    fun clickOnCalendarItem() {
        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItem<DashboardProgramViewHolder>(
                    hasDescendant(withText(containsString(DATE_OF_BIRTH))),
                    clickChildViewWithId(R.id.inputEditText)
                )
            )
    }

    fun checkActiveAndPastEnrollmentDetails(enrollmentListUIModel: EnrollmentListUIModel) {
        checkHeaderAndProgramDetails(
            enrollmentListUIModel,
            ACTIVE_PROGRAMS,
            1,
            2,
            enrollmentListUIModel.currentEnrollmentDate
        )
        checkHeaderAndProgramDetails(
            enrollmentListUIModel,
            PAST_PROGRAMS,
            3,
            4,
            enrollmentListUIModel.pastEnrollmentDate
        )
    }

    private fun checkHeaderAndProgramDetails(
        enrollmentListUIModel: EnrollmentListUIModel,
        programStatus: String,
        headerPosition: Int,
        programPosition: Int,
        enrollmentDay: String
    ) {
        onView(withId(R.id.recycler)).check(
            matches(
                atPosition(
                    headerPosition,
                    withText(programStatus)
                )
            )
        )
        onView(withId(R.id.recycler)).check(
            matches(
                allOf(
                    atPosition(
                        programPosition, allOf(
                            hasDescendant(withText(enrollmentListUIModel.program)),
                            hasDescendant(withText(enrollmentListUIModel.orgUnit)),
                            hasDescendant(withText(enrollmentDay))
                        )
                    )
                )
            )
        )
    }

    fun clickOnEnrolledProgram(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<DashboardProgramViewHolder>(position, click())
            )
    }

    fun typeOnDateParameterWithLabel(label: String, dateValue: String) {
        composeTestRule.apply {
            onNode(
                hasTestTag("INPUT_DATE_TIME_TEXT_FIELD") and hasAnySibling(hasText(label)),
                useUnmergedTree = true,
            ).performTextReplacement(dateValue)
        }
    }

    companion object {
        const val ACTIVE_PROGRAMS = "Active programs"
        const val PAST_PROGRAMS = "Past programs"
        const val DATE_OF_BIRTH = "Date of birth"
    }
}