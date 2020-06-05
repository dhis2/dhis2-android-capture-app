package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.common.matchers.clickOnTab
import org.dhis2.common.matchers.isToast
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.common.viewactions.scrollToBottomRecyclerView
import org.dhis2.common.viewactions.typeChildViewWithId
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
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
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            0,
                            hasDescendant(
                                hasSibling(
                                    allOf(
                                        withId(R.id.event_name),
                                        withText(eventName)
                                    )
                                )
                            )
                        )
                    )
                )
            )
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

    fun checkTEIIsDelete() {
        onView(withId(R.id.scrollView)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    not(hasDescendant(withText("Olvia Watts")))
                )
            )
        )
    }

    fun clickOnTEI() {
        onView(withId(R.id.scrollView)).perform(
            scrollTo<SearchTEViewHolder>(
                hasDescendant(
                    withText(
                        "Olvia Watts"
                    )
                )
            ),
            click()
        )
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
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(1, hasDescendant(withText(enrollmentUIModel.enrollmentDate)))
                    )
                )
            )

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(2, hasDescendant(withText(enrollmentUIModel.birthday)))
                )
            )
        )

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(3, hasDescendant(withText(enrollmentUIModel.orgUnit)))
                )
            )
        )

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(
                        4,
                        hasDescendant(
                            allOf(
                                withId(R.id.latitude),
                                withText(enrollmentUIModel.latitude)
                            )
                        )
                    )
                )
            )
        )

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(
                        4,
                        hasDescendant(
                            allOf(
                                withId(R.id.longitude), withText(enrollmentUIModel.longitude)
                            )
                        )
                    )
                )
            )
        )

        onView(withId(R.id.fieldRecycler))
            .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(6, click()))

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(2, hasDescendant(withText(enrollmentUIModel.name)))
                )
            )
        )

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(3, hasDescendant(withText(enrollmentUIModel.lastName)))
                )
            )
        )

        onView(withId(R.id.fieldRecycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(4, hasDescendant(withText(enrollmentUIModel.sex)))
                )
            )
        )
    }

    fun clickOnScheduleNew() {
        onView(withId(R.id.schedulenew)).perform(click())
    }

    fun clickOnMenuProgramEnrollments() {
        onView(withText("Program enrollments")).perform(click())
    }
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

    fun clickOnCreateNewEvent() {
        onView(withId(R.id.addnew)).perform(click())
    }

    fun checkEventWasCreatedAndOpen(eventName: String, position: Int) {
        onView(withId(R.id.tei_recycler))
            .check(
                matches(
                    allOf(
                        isDisplayed(), isNotEmpty(),
                        atPosition(
                            position,
                            allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(withText(R.string.event_open))
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
                            position,
                            allOf(
                                hasDescendant(withText(eventName)),
                                hasDescendant(withText(R.string.event_completed))
                            )
                        )
                    )
                )
            )
    }

    fun clickOnPersonAttributes(position: Int) {
        onView(withId(R.id.fieldRecycler))
            .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(position, click()))
    }

    fun scrollToBottomProgramForm() {
        onView(withId(R.id.fieldRecycler)).perform(scrollToBottomRecyclerView())
    }

    fun scrollToBottomEventForm() {
        onView(withId(R.id.formRecycler)).perform(scrollToBottomRecyclerView())
    }

    fun typeOnRequiredTextField(text: String, position: Int) {
        onView(withId(R.id.fieldRecycler))
            .perform(
                actionOnItemAtPosition<DashboardProgramViewHolder>(
                    position,
                    typeChildViewWithId(text, R.id.input_editText)
                )
            )
    }
}
