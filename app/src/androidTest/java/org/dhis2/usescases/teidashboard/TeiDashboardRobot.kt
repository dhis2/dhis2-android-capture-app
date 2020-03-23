package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import kotlinx.android.synthetic.main.fragment_notes.view.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.clickOnTab


fun teiDashboardRobot(teiDashboardRobot: TeiDashboardRobot.() -> Unit) {
    TeiDashboardRobot().apply {
        teiDashboardRobot
    }
}

class TeiDashboardRobot: BaseRobot () {

    fun clickOnPinTab() {
        onView(clickOnTab(3)).perform(click())
    }

}