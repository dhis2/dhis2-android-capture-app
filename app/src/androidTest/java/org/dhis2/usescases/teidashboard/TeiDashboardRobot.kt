package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.clickOnTab
import org.hamcrest.Matchers.not


fun teiDashboardRobot(teiDashboardRobot: TeiDashboardRobot.() -> Unit) {
    TeiDashboardRobot().apply {
        teiDashboardRobot()
    }
}

class TeiDashboardRobot: BaseRobot () {
    fun clickOnPinTab() {
        //tab_layout
        onView(clickOnTab(3)).perform(click())
        Thread.sleep(500)
    }

    fun clickOnMenu() {
        onView(withId(R.id.moreOptions)).perform(click())
        Thread.sleep(500)
    }

    fun clickOnMenuReOpen() {
        //onView(withId(R.id.activate)).check(matches(isDisplayed()))
                //.perform(click())
        onView(withText("re-open")).perform(click())
                //.check(matches(isDisplayed()))
        Thread.sleep(500)
    }

    fun checkLockIconIsDisplay() {
        onView(withId(R.id.program_lock_text)).check(matches(withText("Completed")))
    }

    fun checkUnlockIconIsDisplay() {
        onView(withId(R.id.program_lock_text)).check(matches(withText("Open")))
    }

    fun checkCanAddEvent() {
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.addnew)).check(matches(isDisplayed()))
    }

    fun clickOnMenuOpen() {
        onView(withText("Complete")).perform(click())
        //onView(withId(R.id.deactivate)).perform(click())
        Thread.sleep(500)
    }

    fun checkCanNotAddEvent() {
        onView(withId(R.id.fab)).check(matches(not(isDisplayed())))
    }
}