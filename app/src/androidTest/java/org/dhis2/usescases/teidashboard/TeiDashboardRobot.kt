package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.clickOnTab
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionViewHolder
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.DashboardProgramViewHolder
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not


fun teiDashboardRobot(teiDashboardRobot: TeiDashboardRobot.() -> Unit) {
    TeiDashboardRobot().apply {
        teiDashboardRobot()
    }
}

class TeiDashboardRobot: BaseRobot () {

    fun clickOnNotesTab() {
        onView(clickOnTab(3)).perform(click())
        //onView(isRoot()).perform(waitForTransitionUntil(R.id.addNoteButton))
        Thread.sleep(500)
    }

    fun clickOnMenu() {
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

    fun clickOnEventWithPosition(position : Int){
        onView(withId(R.id.tei_recycler))
                .perform(actionOnItemAtPosition<DashboardProgramViewHolder>(position, click()))
    }

    fun clickOnFab(){
        onView(withId(R.id.fab)).perform(click())
    }

    fun clickOnReferral(){
        onView(withId(R.id.referral)).perform(click())
    }

    fun clickOnFirstReferralEvent(){
        onView(withId(R.id.recycler_view))
                .perform(actionOnItemAtPosition<ProgramStageSelectionViewHolder>(0, click()))
    }

    fun checkEventIsCreated(eventName:String){

    }

    fun clickOnMenuDeactivate() {
        onView(withText(R.string.deactivate)).perform(click())
    }

    fun checkCanNotAddEvent() {
        onView(withId(R.id.fab)).check(matches(not(isDisplayed())))
    }

    fun clickOnShareButton() {
        onView(withId(R.id.shareContainer)).perform(click())
    }

    fun checkNumberOfQR() {
        onView(withId(R.id.page)).check(matches(withText("8")))
    }

    fun clickOnNextQR() {
        onView(withId(R.id.next)).perform(click())
    }
}