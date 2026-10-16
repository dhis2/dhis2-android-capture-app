package org.dhis2.usescases.teidashboard.robot

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.ChartMatchers
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.viewactions.clickChildViewWithId
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsNot.not

fun analyticsRobot(analyticsRobot: AnalyticsRobot.() -> Unit) {
    AnalyticsRobot().apply {
        analyticsRobot()
    }
}

class AnalyticsRobot : BaseRobot() {
    fun checkGraphType(chartPosition: Int, charType: ChartType) {
        onView(withId(R.id.indicators_recycler)).check(matches(atPosition(chartPosition, hasDescendant(ChartMatchers.hasChartType(charType)))))
    }

    fun checkChartName(chartPosition: Int, chartName: String) {
        onView(withId(R.id.indicators_recycler)).check(
            matches(
                atPosition(
                    chartPosition,
                    hasDescendant(allOf(withId(R.id.chart_title), withText(chartName))),
                ),
            ),
        )
    }

    fun clickChartVisualizationMenu(chartPosition: Int) {
        onView(withId(R.id.indicators_recycler))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(chartPosition, clickChildViewWithId(R.id.chartVisualizationButton)))
    }

    fun clickViewAsBar() {
        onView(withText(R.string.show_bar_graph)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickViewAsTable() {
        onView(withText(R.string.show_table_graph)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickViewAsValue() {
        onView(withText(R.string.show_table_value)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickOrgUnitFilter() {
        onView(withText(R.string.orgUnit)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickOrgUnitSelection() {
        onView(withText(R.string.selection)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickPeriodFilter() {
        onView(withText(R.string.period)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickDailyPeriod() {
        onView(withText(R.string.daily)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickToday() {
        onView(withText(R.string.today)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickReset() {
        onView(withText(R.string.reset)).inRoot(isPlatformPopup()).perform(click())
    }

    fun checkFilterBadgeVisible(chartPosition: Int) {
        onView(withId(R.id.indicators_recycler)).check(
            matches(
                atPosition(
                    chartPosition,
                    hasDescendant(
                        allOf(withId(R.id.chartFilters), withEffectiveVisibility(Visibility.VISIBLE)),
                    ),
                ),
            ),
        )
    }

    fun checkFilterBadgeGone(chartPosition: Int) {
        onView(withId(R.id.indicators_recycler)).check(
            matches(
                atPosition(
                    chartPosition,
                    hasDescendant(
                        allOf(withId(R.id.chartFilters), withEffectiveVisibility(Visibility.GONE)),
                    ),
                ),
            ),
        )
    }

    fun checkNoErrorIcon(chartPosition: Int) {
        onView(withId(R.id.indicators_recycler)).check(
            matches(
                atPosition(
                    chartPosition,
                    hasDescendant(
                        not(allOf(withId(R.id.error_data_icon), withEffectiveVisibility(Visibility.VISIBLE))),
                    ),
                ),
            ),
        )
    }

    fun checkNoVisualisationError(chartPosition: Int) {
        checkNoErrorIcon(chartPosition)
        checkFilterBadgeVisible(chartPosition)
    }
}
