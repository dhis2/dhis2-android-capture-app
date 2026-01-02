package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.usescases.flow.teiFlow.entity.EnrollmentListUIModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity
import org.dhis2.usescases.teiDashboard.teiProgramList.ui.PROGRAM_TO_ENROLL
import org.hamcrest.CoreMatchers.allOf

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
        composeTestRule.waitForIdle()
    }

    fun checkEnrollmentListActivityIsLaunched() {
        Intents.intended(allOf(hasComponent(TeiProgramListActivity::class.java.name)))
    }


    fun clickOnAcceptInDatePicker() {
        waitForView(withId(R.id.acceptBtn)).perform(click())
    }

    fun clickOnSaveEnrollment() {
        waitForView(withId(R.id.save)).perform(click())
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
            val dateTextFieldNode = onNode(
                hasTestTag("INPUT_DATE_TIME_TEXT_FIELD") and hasAnySibling(hasText(label)),
                useUnmergedTree = true,
            )
            dateTextFieldNode.performTextReplacement(dateValue)
            dateTextFieldNode.performImeAction()
        }
        Espresso.closeSoftKeyboard()
    }

    @OptIn(ExperimentalTestApi::class)
    fun openFormSection(personAttribute: String) {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(hasText(personAttribute, true), TIMEOUT)
        composeTestRule.onNodeWithText(personAttribute).performClick()
        composeTestRule.waitForIdle()
    }

    fun typeOnInputDateField(dateValue: String, title: String) {
        composeTestRule.waitForIdle()
        composeTestRule.apply {
            onNode(
                hasTestTag(
                    "INPUT_DATE_TIME_TEXT_FIELD"
                ) and hasAnySibling(
                    hasText(title)
                ),
                useUnmergedTree = true,
            ).performTextReplacement(dateValue)
            onNode(
                hasTestTag(
                    "INPUT_DATE_TIME_TEXT_FIELD"
                ) and hasAnySibling(
                    hasText(title)
                ),
                useUnmergedTree = true,
            ).performImeAction()
        }
        Espresso.closeSoftKeyboard()
    }

    companion object {
        const val ACTIVE_PROGRAMS = "Active programs"
        const val PAST_PROGRAMS = "Past programs"
    }
}
