package org.dhis2.usescases.teidashboard.robot

import android.content.Context
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.common.matchers.isToast
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.usescases.event.entity.EventStatusUIModel
import org.dhis2.usescases.event.entity.TEIProgramStagesUIModel
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.StageViewHolder
import org.dhis2.usescases.teiDashboard.ui.STATE_INFO_BAR_TEST_TAG
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.dhis2.usescases.teidashboard.entity.UpperEnrollmentUIModel
import org.dhis2.utils.dialFloatingActionButton.FAB_ID
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher

fun teiDashboardRobot(teiDashboardRobot: TeiDashboardRobot.() -> Unit) {
    TeiDashboardRobot().apply {
        teiDashboardRobot()
    }
}

class TeiDashboardRobot : BaseRobot() {

    fun goToNotes() {
        onView(withId(R.id.navigation_notes)).perform(click())
        Thread.sleep(500)
    }

    fun clickOnSync() {
        onView(withId(R.id.syncButton)).perform(click())
    }

    fun goToRelationships() {
        onView(withId(R.id.navigation_relationships)).perform(click())
        Thread.sleep(500)
    }

    fun goToAnalytics() {
        onView(withId(R.id.navigation_analytics)).perform(click())
        Thread.sleep(500)
    }

    fun clickOnMenuMoreOptions() {
        waitForView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnMenuReOpen() {
        onView(withText(R.string.re_open)).perform(click())
    }

    fun checkCancelledStateInfoBarIsDisplay(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(STATE_INFO_BAR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrollment cancelled").assertIsDisplayed()
    }

    fun checkCanAddEvent() {
        onView(withId(FAB_ID)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click())
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val addNewTag = targetContext.resources.getString(R.string.add_new)
        onView(withTagValue(equalTo(addNewTag))).check(matches(isDisplayed()))
    }

    fun clickOnEventWithPosition(position: Int) {
        onView(withId(R.id.tei_recycler))
            .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(position, click()))
    }

    fun clickOnEventWith(eventDate: String, orgUnit: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<DashboardProgramViewHolder>(
                    allOf(
                        hasDescendant(withText(eventDate)), hasDescendant(
                            withText(orgUnit)
                        )
                    ), click()
                )
            )
    }

    fun clickOnEventWith(eventName: String, eventStatus: Int, date: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<DashboardProgramViewHolder>(
                    allOf(
                        hasDescendant(withText(eventName)),
                        hasDescendant(withText(eventStatus)),
                        hasDescendant(withText(date))
                    ),
                    click()
                )
            )
    }

    fun clickOnGroupEventByName(name: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<DashboardProgramViewHolder>(
                    hasDescendant(withText(name)),
                    click()
                )
            )
    }

    fun clickOnFab() {
        onView(withId(FAB_ID)).perform(click())
    }

    fun clickOnReferral() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val referalTag = targetContext.resources.getString(R.string.referral)
        onView(withTagValue(equalTo(referalTag))).perform(click())
    }

    fun checkCannotAddMoreEventToastIsShown() {
        onView(withText(R.string.program_not_allow_events)).inRoot(isToast())
            .check(matches(isDisplayed()))
    }

    fun clickOnFirstReferralEvent() {
        onView(withId(R.id.recycler_view))
            .check(matches(allOf(atPosition(0, hasDescendant(withText("Lab monitoring"))))))
            .perform(actionOnItemAtPosition<ProgramStageSelectionViewHolder>(0, click()))
    }

    fun clickOnReferralOption(composeTestRule: ComposeTestRule, oneTime: String) {
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
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            0, hasDescendant(
                                hasSibling(
                                    allOf(
                                        withId(R.id.programStageName),
                                        withText(eventName)
                                    )
                                )
                            )
                        )
                    )
                )
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
        onView(withText(R.string.deactivate)).perform(click())
    }

    fun clickOnMenuComplete() {
        onView(withText(R.string.complete)).perform(click())
    }

    fun checkCompleteStateInfoBarIsDisplay(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(STATE_INFO_BAR_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrollment completed").assertIsDisplayed()
    }


    fun checkCanNotAddEvent() {
        onView(withId(FAB_ID)).check(matches(not(isDisplayed())))
    }

    fun clickOnShareButton() {
        onView(withText(R.string.share)).perform(click())
    }

    fun clickOnNextQR() {
        var qrLenght = 1

        while (qrLenght < 8) {
            onView(withId(R.id.next)).perform(click())
            qrLenght++
        }
    }

    fun clickOnMenuDeleteTEI() {
        onView(withText(R.string.dashboard_menu_delete_person)).perform(click())
    }

    fun checkUpperInfo(upperInformation: UpperEnrollmentUIModel) {
        onView(withId(R.id.incident_date))
            .check(matches(withText(upperInformation.incidentDate)))
        onView(withId(R.id.enrollment_date))
            .check(matches(withText(upperInformation.enrollmentDate)))
        onView(withId(R.id.org_unit))
            .check(matches(withText(upperInformation.orgUnit)))
    }

    fun clickOnSeeDetails() {
        onView(withId(R.id.editButton)).perform(click())
    }

    fun checkFullDetails(enrollmentUIModel: EnrollmentUIModel) {
        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.enrollmentDate)))))))

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.birthday)))))))

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.orgUnit)))))))

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.latitude)))))))

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.longitude)))))))


        onView(withId(R.id.recyclerView))
            .perform(
                actionOnItemAtPosition<DashboardProgramViewHolder>(
                    6,
                    clickChildViewWithId(R.id.section_details)
                )
            )

        waitToDebounce(2000)

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.name)))))))

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.lastName)))))))

        onView(withId(R.id.recyclerView)).check(matches(not(recyclerChildViews(hasItem(hasDescendant(withText(enrollmentUIModel.sex)))))))

    }

    private fun recyclerChildViews(matcher: Matcher<View>): BoundedMatcher<View?, RecyclerView> =
        object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("RecyclerView child views: ")
                matcher.describeTo(description)
            }

            override fun matchesSafely(recyclerView: RecyclerView): Boolean =
                matcher.matches(sequence {
                    val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder> =
                        recyclerView.adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
                    for (position in 0..<adapter.itemCount) {
                        val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(position))
                        adapter.onBindViewHolder(holder, position)
                        yield(holder.itemView)
                    }
                })
        }
    fun clickOnScheduleNew() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val scheduleTag = targetContext.resources.getString(R.string.schedule_new)
        onView(withTagValue(equalTo(scheduleTag))).perform(click())
    }

    fun clickOnMenuProgramEnrollments() {
        onView(withText(R.string.program_selector)).perform(click())
    }

    fun clickOnCreateNewEvent() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val addNewTag = targetContext.resources.getString(R.string.add_new)
        onView(withTagValue(equalTo(addNewTag))).perform(click())
    }

    fun checkEventWasCreatedAndOpen(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position, allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(
                                    withTagValue(
                                        equalTo(
                                            R.drawable.ic_event_status_open
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    fun checkEventWasCreatedAndClosed(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position, allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(
                                    withTagValue(
                                        anyOf(
                                            equalTo(R.drawable.ic_event_status_complete),
                                            equalTo(R.drawable.ic_event_status_complete_read)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    fun clickOnMenuDeleteEnrollment() {
        onView(withText(R.string.dashboard_menu_delete_enrollment)).perform(click())
    }

    fun clickOnGroupByStage() {
        onView(withText(R.string.group_events_by_stage)).perform(click())
    }

    fun clickOnTimelineEvents() {
        onView(withText(R.string.show_events_timeline)).perform(click())
    }

    fun checkEventWasScheduled(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position, allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(
                                    withTagValue(
                                        anyOf(
                                            equalTo(R.drawable.ic_event_status_schedule),
                                            equalTo(R.drawable.ic_event_status_schedule_read)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    private fun checkEventIsClosed(position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position, hasDescendant(
                                withTagValue(
                                    anyOf(
                                        equalTo(R.drawable.ic_event_status_open_read),
                                        equalTo(R.drawable.ic_event_status_overdue_read),
                                        equalTo(R.drawable.ic_event_status_complete_read),
                                        equalTo(R.drawable.ic_event_status_skipped_read),
                                        equalTo(R.drawable.ic_event_status_schedule_read)
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    private fun checkEventIsOpen(position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position,
                            hasDescendant(
                                withTagValue(
                                    anyOf(
                                        equalTo(R.drawable.ic_event_status_open),
                                        equalTo(R.drawable.ic_event_status_open_read)
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    private fun checkEventIsCompleted(position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position,
                            hasDescendant(
                                withTagValue(
                                    anyOf(
                                        equalTo(R.drawable.ic_event_status_complete),
                                        equalTo(R.drawable.ic_event_status_complete_read)
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    private fun checkEventIsInactivate(position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(), atPosition(
                            position, hasDescendant(
                                withTagValue(
                                    anyOf(
                                        equalTo(R.drawable.ic_event_status_open_read),
                                        equalTo(R.drawable.ic_event_status_overdue_read),
                                        equalTo(R.drawable.ic_event_status_complete_read),
                                        equalTo(R.drawable.ic_event_status_skipped_read),
                                        equalTo(R.drawable.ic_event_status_schedule_read)
                                    )
                                )
                            )
                        )
                    )
                )
            )
    }

    fun checkAllEventsAreInactive(totalEvents: Int) {
        var event = 0
        while (event < totalEvents) {
            checkEventIsInactivate(event)
            event++
        }
    }

    fun checkAllEventsAreOpened(totalEvents: Int) {
        var event = 0
        while (event < totalEvents) {
            checkEventIsOpen(event)
            event++
        }
    }

    fun checkAllEventsCompleted(totalEvents: Int) {
        var event = 0
        while (event < totalEvents) {
            checkEventIsCompleted(event)
            event++
        }
    }

    fun checkAllEventsAreClosed(totalEvents: Int) {
        var event = 0
        while (event < totalEvents) {
            checkEventIsClosed(event)
            event++
        }
    }

    fun clickOnStageGroup(programStageName: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<StageViewHolder>(
                    hasDescendant(withText(programStageName)),
                    click()
                )
            )
    }

    fun clickOnEventGroupByStage(eventDate: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<EventViewHolder>(
                    hasDescendant(
                        allOf(
                            withText(eventDate),
                            withId(R.id.event_date)
                        )
                    ), click()
                )
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
                                hasDescendant(withText(firstProgramStage.events))
                            )
                        ),
                        hasItem(
                            allOf(
                                hasDescendant(withText(secondProgramStage.name)),
                                hasDescendant(withText(secondProgramStage.events))
                            )
                        ),
                        hasItem(
                            allOf(
                                hasDescendant(withText(thirdProgramStage.name)),
                                hasDescendant(withText(thirdProgramStage.events))
                            )
                        )
                    )
                )
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
                            hasDescendant(withTagValue(equalTo(status)))
                        )
                    )
                )
            )
    }

    fun clickOnEventGroupByStageUsingOU(orgUnit: String) {
        onView(withId(R.id.tei_recycler))
            .perform(
                actionOnItem<EventViewHolder>(
                    hasDescendant(
                        allOf(
                            withText(orgUnit),
                            withId(R.id.organisationUnit)
                        )
                    ), click()
                )
            )
    }

    fun checkProgramStageIsHidden(stageName: String) {
        onView(withId(R.id.tei_recycler))
            .check(matches(not(hasItem(hasDescendant(withText(stageName))))))
    }

    companion object {
        const val OPEN_EVENT_STATUS = R.string.event_open
        const val OVERDUE_EVENT_STATUS = R.string.event_overdue
    }
}
