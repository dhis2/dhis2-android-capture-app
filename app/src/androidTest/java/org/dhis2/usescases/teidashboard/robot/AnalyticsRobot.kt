package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.ChartMatchers
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition

fun analyticsRobot(analyticsRobot: AnalyticsRobot.() -> Unit) {
    AnalyticsRobot().apply {
        analyticsRobot()
    }
}

class AnalyticsRobot : BaseRobot() {
    fun checkGraphType(chartPosition:Int, charType:ChartType){
        onView(withId(R.id.indicators_recycler)).check(matches(atPosition(chartPosition, hasDescendant(ChartMatchers.hasChartType(charType)))))
    }
}
