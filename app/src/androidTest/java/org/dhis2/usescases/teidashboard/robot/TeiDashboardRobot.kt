package org.dhis2.usescases.teidashboard.robot

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
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
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.usescases.event.entity.EventStatusUIModel
import org.dhis2.usescases.event.entity.TEIProgramStagesUIModel
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder
import org.dhis2.usescases.teiDashboard.ui.STATE_INFO_BAR_TEST_TAG
import org.dhis2.usescases.teiDashboard.ui.TEST_ADD_EVENT_BUTTON
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.dhis2.usescases.teidashboard.entity.UpperEnrollmentUIModel
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

class TeiDashboardRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun goToNotes() {
        composeTestRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.navigation_notes
            )
        ).performClick()
        Thread.sleep(500)
    }

    fun goToRelationships() {
        composeTestRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.navigation_relations
            )
        ).performClick()
        Thread.sleep(500)
    }

    fun goToAnalytics() {
        composeTestRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.navigation_analytics
            )
        ).performClick()
        Thread.sleep(500)
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
        composeTestRule.onNodeWithTag(STATE_INFO_BAR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrollment cancelled").assertIsDisplayed()
    }

    fun clickOnEventWithTitle(title: String) {
        composeTestRule.onNodeWithText(title).performClick()
    }

    fun clickOnEventWith(searchParam: String) {
        composeTestRule.onAllNodesWithText(searchParam, useUnmergedTree = true).onFirst()
            .performClick()
    }

    fun clickOnFab() {
        composeTestRule.onNodeWithTag(TEST_ADD_EVENT_BUTTON, useUnmergedTree = true).performClick()
    }

    fun clickOnReferral() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val referalTag = targetContext.resources.getString(R.string.refer)
        composeTestRule.onNodeWithText(referalTag, true).performClick()
    }

    fun clickOnFirstReferralEvent() {
        onView(withId(R.id.recycler_view))
            .check(matches(allOf(atPosition(0, hasDescendant(withText("Lab monitoring"))))))
            .perform(actionOnItemAtPosition<ProgramStageSelectionViewHolder>(0, click()))
    }

    fun clickOnReferralOption(oneTime: String) {
        composeTestRule.onNodeWithText(oneTime).performClick()
    }

    fun clickOnReferralNextButton() {
        waitForView(withId(R.id.action_button)).perform(click())
    }

    fun checkEventWasCreated(eventName: String) {
        onView(withId(R.id.tei_recycler))
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

    fun checkEventWasCreatedWithDate(eventName: String, eventDate: String) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        isNotEmpty(),
                        atPosition(
                            1,
                            hasDescendant(
                                allOf(
                                    hasSibling(
                                        allOf(
                                            withId(R.id.programStageName),
                                            withText(eventName),
                                        ),
                                    ),
                                    hasSibling(
                                        allOf(
                                            withId(R.id.event_date),
                                            withText(eventDate),
                                        ),
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
            composeTestRule.onNodeWithText(getString(R.string.complete)).performClick()
        }
    }

    fun checkCompleteStateInfoBarIsDisplay() {
        composeTestRule.onNodeWithTag(STATE_INFO_BAR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrollment completed").assertIsDisplayed()
    }

    fun checkCanNotAddEvent() {
        composeTestRule.onNodeWithTag(TEST_ADD_EVENT_BUTTON, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    fun clickOnShareButton() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.share)).performClick()
        }
    }

    fun clickOnNextQR() {
        var qrLenght = 1

        while (qrLenght < 8) {
            waitForView(withId(R.id.next))
            onView(withId(R.id.next)).perform(click())
            qrLenght++
        }
    }

    fun clickOnMenuDeleteTEI() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            composeTestRule.onNodeWithText(getString(R.string.dashboard_menu_delete_person)).performClick()
        }
    }

    fun checkUpperInfo(upperInformation: UpperEnrollmentUIModel) {
        onView(withId(R.id.org_unit))
            .check(matches(withText(upperInformation.orgUnit)))
    }

    fun clickOnSeeDetails() {
        onView(withId(R.id.editButton)).perform(click())
    }

    fun checkFullDetails(enrollmentUIModel: EnrollmentUIModel) {
        composeTestRule.apply {
            onNode(
                hasText(
                    enrollmentUIModel.enrollmentDate,
                ) and hasAnySibling(
                    hasText("Date of enrollment *"),
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

            onNodeWithText(enrollmentUIModel.orgUnit).assertIsDisplayed()
            onNodeWithText("Latitude: ${enrollmentUIModel.latitude}").assertIsDisplayed()
            onNodeWithText("Longitude: ${enrollmentUIModel.longitude}").assertIsDisplayed()
            onNodeWithText("Next").performScrollTo()
            onNodeWithText("Next").performClick()

            onNodeWithText(enrollmentUIModel.name).assertIsDisplayed()
            onNodeWithText(enrollmentUIModel.lastName).assertIsDisplayed()
            onNodeWithText(enrollmentUIModel.sex).assertIsDisplayed()
        }
    }

    fun clickOnScheduleNew() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val scheduleTag = targetContext.resources.getString(R.string.schedule_new)
        composeTestRule.onNodeWithTag(scheduleTag, useUnmergedTree = true).performClick()
    }

    fun clickOnMenuProgramEnrollments() {
        with(InstrumentationRegistry.getInstrumentation().targetContext) {
            val programSelectorLabel = getString(R.string.more_enrollments)
            composeTestRule.onNodeWithText(programSelectorLabel).performClick()
        }
    }

    fun checkEventWasCreatedAndClosed(eventName: String) {
        composeTestRule.onNodeWithText(eventName).assertIsDisplayed()
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val viewOnlyText = targetContext.resources.getString(R.string.view_only)
        composeTestRule.onNodeWithText(viewOnlyText).assertDoesNotExist()
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

    fun clickOnStageGroup(programStageName: String) {
        composeTestRule.onNodeWithText(programStageName).performClick()
    }

    fun clickOnEventGroupByStage(eventDate: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<EventViewHolder>(
                    hasDescendant(
                        allOf(
                            withText(eventDate),
                            withId(R.id.event_date),
                        ),
                    ),
                    click(),
                ),
            )
    }

    fun checkEventWasDeletedStageGroup(teiProgramStages: TEIProgramStagesUIModel) {
        val firstProgramStage = teiProgramStages.programStage_first
        val secondProgramStage = teiProgramStages.programStage_second
        val thirdProgramStage = teiProgramStages.programStage_third

        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        hasItem(
                            allOf(
                                hasDescendant(withText(firstProgramStage.name)),
                                hasDescendant(withText(firstProgramStage.events)),
                            ),
                        ),
                        hasItem(
                            allOf(
                                hasDescendant(withText(secondProgramStage.name)),
                                hasDescendant(withText(secondProgramStage.events)),
                            ),
                        ),
                        hasItem(
                            allOf(
                                hasDescendant(withText(thirdProgramStage.name)),
                                hasDescendant(withText(thirdProgramStage.events)),
                            ),
                        ),
                    ),
                ),
            )
    }

    fun checkEventStateStageGroup(eventDetails: EventStatusUIModel) {
        var status = R.drawable.ic_event_status_open
        when (eventDetails.status) {
            "Open" -> status = R.drawable.ic_event_status_open
            "Overdue" -> status = R.drawable.ic_event_status_overdue
            "Event Completed" -> status = R.drawable.ic_event_status_complete
            "Skip" -> status = R.drawable.ic_event_status_skipped
            "Schedule" -> status = R.drawable.ic_event_status_schedule
        }

        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    hasItem(
                        allOf(
                            hasDescendant(withText(eventDetails.date)),
                            hasDescendant(withText(eventDetails.orgUnit)),
                            hasDescendant(withTagValue(equalTo(status))),
                        ),
                    ),
                ),
            )
    }

    fun clickOnEventGroupByStageUsingDate(dueDate: String) {
        composeTestRule.onNodeWithText(dueDate).performClick()
    }

    fun clickOnConfirmDeleteTEI() {
        composeTestRule.onNodeWithText("Delete").performClick()
    }

    fun clickOnConfirmDeleteEnrollment() {
        composeTestRule.onNodeWithText("Remove").performClick()
    }

    fun checkEnrollmentDate(enrollmentDate: DateRegistrationUIModel) {
        composeTestRule.onNode(
            hasText(
                "Date of enrollment:  0${enrollmentDate.day}/0${enrollmentDate.month}/${enrollmentDate.year}",
                true
            ),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }
}
