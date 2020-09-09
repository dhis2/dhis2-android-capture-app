package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.common.matchers.clickOnTab
import org.dhis2.common.matchers.isToast
import org.dhis2.usescases.event.entity.TEIProgramStagesUIModel
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramViewHolder
import org.dhis2.usescases.teidashboard.entity.EnrollmentUIModel
import org.dhis2.usescases.teidashboard.entity.UpperEnrollmentUIModel
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

fun teiDashboardRobot(teiDashboardRobot: TeiDashboardRobot.() -> Unit) {
    TeiDashboardRobot().apply {
        teiDashboardRobot()
    }
}

class TeiDashboardRobot : BaseRobot() {

    fun clickOnNotesTab() {
        onView(clickOnTab(3)).perform(click())
        // onView(isRoot()).perform(waitForTransitionUntil(R.id.addNoteButton))
        Thread.sleep(500)
    }

    fun clickOnRelationshipTab() {
        onView(clickOnTab(2)).perform(click())
        // onView(isRoot()).perform(waitForTransitionUntil(R.id.addNoteButton))
        Thread.sleep(500)
    }

    fun clickOnIndicatorsTab() {
        onView(clickOnTab(1)).perform(click())
        // onView(isRoot()).perform(waitForTransitionUntil(R.id.addNoteButton))
        Thread.sleep(500)
    }

    fun clickOnMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnMenuReOpen() {
        onView(withText(R.string.re_open)).perform(click())
    }

    fun checkLockIconIsDisplay() {
        onView(withId(R.id.program_lock_text)).check(matches(withText(R.string.cancelled)))
    }

    fun checkUnlockIconIsDisplay() {
        onView(withId(R.id.program_lock_text)).check(matches(withText(R.string.event_open)))
    }

    fun checkCanAddEvent() {
        onView(withId(R.id.fab)).check(matches(allOf(isDisplayed(), isEnabled()))).perform(click())
        onView(withId(R.id.addnew)).check(matches(isDisplayed()))
    }

    fun clickOnEventWithPosition(position: Int) {
        onView(withId(R.id.tei_recycler))
            .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(position, click()))
    }

    fun clickOnFab() {
        onView(withId(R.id.fab)).perform(click())
    }

    fun clickOnReferral() {
        onView(withId(R.id.referral)).perform(click())
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

    fun clickOnReferralOption() {
        onView(withId(R.id.one_time)).perform(click())
    }

    fun clickOnReferralNextButton() {
        onView(withId(R.id.action_button)).perform(click())
    }

    fun checkEventCreatedToastIsShown() {
        onView(withText(R.string.event_created)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    fun checkEventWasCreated(eventName: String) {
        onView(withId(R.id.tei_recycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(0, hasDescendant(
                    hasSibling(allOf(withId(R.id.event_name), withText(eventName))))))))
    }

    fun clickOnMenuDeactivate() {
        onView(withText(R.string.deactivate)).perform(click())
    }

    fun clickOnMenuComplete() {
        onView(withText(R.string.complete)).perform(click())
    }

    fun checkLockCompleteIconIsDisplay() {
        onView(withId(R.id.program_lock_text)).check(matches(withText(R.string.completed)))
    }

    fun checkCanNotAddEvent() {
        onView(withId(R.id.fab)).check(matches(not(isDisplayed())))
    }

    fun clickOnShareButton() {
        onView(withId(R.id.shareContainer)).perform(click())
    }

    fun clickOnNextQR() {
        var qrLenght = 1

        while (qrLenght < 8) {
            onView(withId(R.id.next)).perform(click())
            qrLenght++
        }
    }

    fun clickOnMenuDeleteTEI() {
        onView(withText(R.string.dashboard_menu_delete_tei)).perform(click())
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
        onView(withId(R.id.viewMore)).perform(click())
    }

    fun checkFullDetails(enrollmentUIModel: EnrollmentUIModel) {
        onView(withId(R.id.fieldRecycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty(),
                    atPosition(1, hasDescendant(withText(enrollmentUIModel.enrollmentDate))))))

        onView(withId(R.id.fieldRecycler)).check(
            matches(allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(2, hasDescendant(withText(enrollmentUIModel.birthday))))))

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(isDisplayed(), isNotEmpty(),
                    atPosition(3, hasDescendant(withText(enrollmentUIModel.orgUnit))))))

        onView(withId(R.id.fieldRecycler)).check(matches(allOf(isDisplayed(), isNotEmpty(),
            atPosition(4, hasDescendant(allOf(withId(R.id.latitude),
                withText(enrollmentUIModel.latitude)))))))

        onView(withId(R.id.fieldRecycler)).check(matches(allOf(isDisplayed(), isNotEmpty(),
                    atPosition(4, hasDescendant(allOf(
                        withId(R.id.longitude), withText(enrollmentUIModel.longitude)))))))

        onView(withId(R.id.fieldRecycler))
            .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(6, click()))

        onView(withId(R.id.fieldRecycler)).check(
            matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(2, hasDescendant(withText(enrollmentUIModel.name))))))

        onView(withId(R.id.fieldRecycler)).check(
            matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(3, hasDescendant(withText(enrollmentUIModel.lastName))))))

        onView(withId(R.id.fieldRecycler)).check(
            matches(allOf(isDisplayed(), isNotEmpty(),
                    atPosition(4, hasDescendant(withText(enrollmentUIModel.sex))))))
    }

    fun clickOnScheduleNew() {
        onView(withId(R.id.schedulenew)).perform(click())
    }

    fun clickOnMenuProgramEnrollments() {
        onView(withText(R.string.program_selector)).perform(click())
    }

    fun clickOnCreateNewEvent() {
        onView(withId(R.id.addnew)).perform(click())
    }

    fun checkEventWasCreatedAndOpen(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(position, allOf(
                    hasDescendant(withText(eventName)),
                    hasDescendant(withText(R.string.event_open)))))))
    }

    fun checkEventWasCreatedAndClosed(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(position, allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(withText(R.string.event_completed)))))))
    }

    fun clickOnMenuDeleteEnrollment() {
        onView(withText(R.string.dashboard_menu_delete_enrollment)).perform(click())
    }

    fun clickOnGroupByStage(){
        onView(withText(R.string.group_events_by_stage)).perform(click())
    }

    fun clickOnTimelineEvents(){
        onView(withText(R.string.show_events_timeline)).perform(click())
    }

    fun checkEventWasScheduled(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(position, allOf(
                    hasDescendant(withText(eventName)),
                    hasDescendant(withText(R.string.event_schedule)))))))
    }

    private fun checkEventIsClosed(position: Int) {
        onView(withId(R.id.tei_recycler))
                .check(matches(allOf(isDisplayed(), isNotEmpty(),
                        atPosition(position, hasDescendant(withText(R.string.program_completed))))))
    }

    private fun checkEventIsOpen(position: Int) {
        onView(withId(R.id.tei_recycler))
                .check(matches(allOf(isDisplayed(), isNotEmpty(),
                        atPosition(position, hasDescendant(withText(R.string.event_open))))))
    }

    private fun checkEventIsCompleted(position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(matches(allOf(isDisplayed(), isNotEmpty(),
                atPosition(position, hasDescendant(withText(R.string.event_completed))))))
    }

    private fun checkEventIsInactivate(position: Int) {
        onView(withId(R.id.tei_recycler))
                .check(matches(allOf(isDisplayed(), isNotEmpty(), atPosition(position, hasDescendant(withText(R.string.program_inactive))))))
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
            .perform(actionOnItem<DashboardProgramViewHolder>(hasDescendant(withText(programStageName)), click()))
    }

    fun clickOnEventGroupByStage(eventName: String) {
        onView(withId(R.id.tei_recycler))
            .perform(actionOnItem<DashboardProgramViewHolder>(hasDescendant(allOf(withText(eventName), withId(R.id.event_name))), click()))
    }

    fun checkEventWasDeletedStageGroup(teiProgramStages: TEIProgramStagesUIModel) {
        val firstProgramStage = teiProgramStages.programStage_first
        val secondProgramStage = teiProgramStages.programStage_second
        val thirdProgramStage = teiProgramStages.programStage_third

        onView(withId(R.id.tei_recycler))
            .check(matches(allOf(
                hasItem(allOf(hasDescendant(withText(firstProgramStage.name)), hasDescendant(withText(firstProgramStage.events)))),
                hasItem(allOf(hasDescendant(withText(secondProgramStage.name)), hasDescendant(withText(secondProgramStage.events)))),
                hasItem(allOf(hasDescendant(withText(thirdProgramStage.name)), hasDescendant(withText(thirdProgramStage.events))))
            )))
    }
}
