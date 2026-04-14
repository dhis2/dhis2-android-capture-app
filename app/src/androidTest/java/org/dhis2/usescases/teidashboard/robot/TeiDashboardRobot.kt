package org.dhis2.usescases.teidashboard.robot

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.dhis2.usescases.teiDashboard.ui.INFO_BAR_TEST_TAG
import org.dhis2.usescases.teiDashboard.ui.TEST_ADD_EVENT_BUTTON
import org.dhis2.usescases.teiDashboard.ui.TEST_ADD_EVENT_BUTTON_IN_TIMELINE
import org.dhis2.usescases.teiDashboard.ui.model.InfoBarType
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo

fun teiDashboardRobot(
    composeTestRule: ComposeTestRule,
    teiDashboardRobot: TeiDashboardRobot.() -> Unit,
) {
    TeiDashboardRobot(composeTestRule).apply {
        teiDashboardRobot()
    }
}

@OptIn(ExperimentalTestApi::class)
class TeiDashboardRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun goToNotes() {
        composeTestRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.navigation_notes
            )
        ).performClick()
        waitForView(withId(R.id.addNoteButton))
    }

    fun goToRelationships() {
        composeTestRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.navigation_relations
            )
        ).performClick()
        Thread.sleep(500)
    }

    @OptIn(ExperimentalTestApi::class)
    fun goToAnalytics() {
        val analyticsText = InstrumentationRegistry.getInstrumentation().targetContext.getString(
            R.string.navigation_analytics
        )
        composeTestRule.waitUntilExactlyOneExists(
            hasText(analyticsText, true),
            TIMEOUT
        )
        composeTestRule.onNodeWithText(analyticsText, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
    }

    fun clickOnMenuMoreOptions() {
        waitForView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnMenuReOpen() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.re_open)).performClick()
        }
    }

    fun checkCancelledStateInfoBarIsDisplay() {
        composeTestRule.onNodeWithTag(INFO_BAR_TEST_TAG + InfoBarType.ENROLLMENT_STATUS.name)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrollment cancelled").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnEventWithTitle(title: String) {
        composeTestRule.waitUntilExactlyOneExists(hasText(title))
        composeTestRule.onNodeWithText(title).performClick()
    }

    fun clickOnFab() {
        composeTestRule.onNodeWithTag(TEST_ADD_EVENT_BUTTON_IN_TIMELINE, useUnmergedTree = true)
            .performClick()
    }

    fun clickOnReferral() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val referalTag = targetContext.resources.getString(R.string.refer)
        composeTestRule.onNodeWithText(referalTag, true).performClick()
    }

    fun clickOnFirstReferralEvent() {
        waitForView(
            allOf(
                withId(R.id.recycler_view),
                hasDescendant(withText("Lab monitoring"))
            )
        ).perform(
            actionOnItemAtPosition<ProgramStageSelectionViewHolder>(0, click())
        )
    }

    fun checkProgramStageSelectionActivityIsLaunched() {
        Intents.intended(allOf(IntentMatchers.hasComponent(ProgramStageSelectionActivity::class.java.name)))
    }

    fun clickOnReferralNextButton() {
        waitForView(withId(R.id.action_button)).perform(click())
    }

    fun checkEventWasCreated(eventName: String) {
        waitForView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isNotEmpty(),
                        atPosition(
                            0,
                            hasDescendant(
                                hasSibling(
                                    allOf(
                                        withId(R.id.programStageName),
                                        withText(eventName),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
    }

    fun clickOnMenuDeactivate() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.deactivate)).performClick()
        }
    }

    fun clickOnMenuComplete() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.waitUntilExactlyOneExists(
                hasText(getString(R.string.complete)),
                TIMEOUT
            )
            composeTestRule.onNodeWithText(getString(R.string.complete)).performClick()
        }
    }

    fun checkCompleteStateInfoBarIsDisplay() {
        composeTestRule.onNodeWithTag(INFO_BAR_TEST_TAG + InfoBarType.ENROLLMENT_STATUS.name)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrollment completed").assertIsDisplayed()
    }

    fun checkCanNotAddEvent() {
        composeTestRule.onNodeWithTag(TEST_ADD_EVENT_BUTTON, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    fun clickOnMenuDeleteTEI() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.dashboard_menu_delete_person))
                .performClick()
        }
    }

    fun clickOnSeeDetails() {
        waitForView(withId(R.id.editButton))
            .perform(click())
    }

    fun checkFullDetails(enrollmentUIModel: EnrollmentUIModel) {
        composeTestRule.apply {
            onNode(
                hasText(
                    enrollmentUIModel.enrollmentDate,
                ) and hasAnySibling(
                    hasText("Enrollment date *"),
                ),
                useUnmergedTree = true,
            ).assertIsDisplayed()

            onNode(
                hasText(
                    enrollmentUIModel.birthday,
                ) and hasAnySibling(
                    hasText("Date of birth *"),
                ),
                useUnmergedTree = true,
            ).assertIsDisplayed()

            onNodeWithText(enrollmentUIModel.name).assertIsDisplayed()
        }
    }

    fun clickOnScheduleNew() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val scheduleTag = targetContext.resources.getString(R.string.schedule) + " event"
        composeTestRule.onNodeWithText(scheduleTag, useUnmergedTree = true).performClick()
    }


    fun clickOnSchedule() {
        composeTestRule.onNodeWithText("Schedule").performClick()
    }

    fun clickOnMenuProgramEnrollments() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val programSelectorLabel = getString(R.string.more_enrollments)
            composeTestRule.waitUntilExactlyOneExists(
                hasText(programSelectorLabel),
                TIMEOUT
            )
            composeTestRule.onNodeWithText(programSelectorLabel).performClick()
        }
    }

    fun clickOnMenuDeleteEnrollment() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val deleteEnrollmentLabel = getString(R.string.remove_from)
            composeTestRule.onNodeWithText(deleteEnrollmentLabel).performClick()
        }
    }

    fun clickOnTimelineEvents() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val timelineLabel = getString(R.string.view_timeline)
            try {
                composeTestRule.waitUntilExactlyOneExists(
                    hasText(timelineLabel),
                    TIMEOUT
                )
                composeTestRule.onNodeWithText(timelineLabel).performClick()
            } catch (e: NoMatchingViewException) {
                checkIfGroupedEventsIsVisible()
            }
        }
    }

    private fun checkIfGroupedEventsIsVisible() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val groupLabel = getString(R.string.group_by_stage)
            composeTestRule.onNodeWithText(groupLabel).assertIsDisplayed()
        }
    }

    fun checkEventWasScheduled(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isNotEmpty(),
                        atPosition(
                            position,
                            allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(
                                    withTagValue(
                                        anyOf(
                                            equalTo(R.drawable.ic_event_status_schedule),
                                            equalTo(R.drawable.ic_event_status_schedule_read),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
    }

    private fun checkEventIsCompleted(position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isNotEmpty(),
                        atPosition(
                            position,
                            hasDescendant(
                                withTagValue(
                                    anyOf(
                                        equalTo(R.drawable.ic_event_status_complete),
                                        equalTo(R.drawable.ic_event_status_complete_read),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
    }

    fun checkAllEventsCompleted(totalEvents: Int) {
        composeTestRule.waitForIdle()
        var event = 0
        while (event < totalEvents) {
            checkEventIsCompleted(event)
            event++
        }
    }

    fun checkAllEventsAreClosed() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val viewOnlyText = targetContext.resources.getString(R.string.view_only)
        composeTestRule.onAllNodes(hasText(viewOnlyText), useUnmergedTree = false)
    }

    fun clickOnConfirmDeleteTEI() {
        composeTestRule.onNodeWithText("Delete").performClick()
    }

    fun clickOnConfirmDeleteEnrollment() {
        composeTestRule.onNodeWithText("Remove").performClick()
    }

    fun typeOnInputDateField(dateValue: String, title: String) {
        composeTestRule.apply {
            onNode(
                hasTestTag(
                    "INPUT_DATE_TIME_TEXT_FIELD"
                ) and hasAnySibling(
                    hasText(title)
                ),
                useUnmergedTree = true,
            ).performTextReplacement(dateValue)
        }
    }
}
