package org.dhis2.usescases.teidashboard

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.clickOnTab


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
}