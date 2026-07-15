package org.dhis2.usescases.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.ChartMatchers
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.usescases.main.program.HOME_ITEMS
import org.dhis2.usescases.main.program.hasPrograms
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsNot.not

private const val HOME_NAVIGATION_TIMEOUT = 15000L

fun homeRobot(
    composeTestRule: ComposeTestRule,
    robotBody: MainRobot.() -> Unit,
) {
    MainRobot(composeTestRule).apply {
        robotBody()
    }
}

class MainRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    @OptIn(ExperimentalTestApi::class)
    fun checkViewIsNotEmpty(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(HOME_ITEMS) and SemanticsMatcher.expectValue(hasPrograms, true),
            HOME_NAVIGATION_TIMEOUT,
        )
        composeTestRule.onNodeWithTag(HOME_ITEMS).assert(
            SemanticsMatcher.expectValue(hasPrograms, true),
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun checkHomeIsDisplayed(composeTestRule: ComposeTestRule) {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(HOME_ITEMS), HOME_NAVIGATION_TIMEOUT)
        composeTestRule.onNodeWithTag(HOME_ITEMS).assertIsDisplayed()
    }

    fun clickChartsTab() {
        composeTestRule.onNodeWithTag("NAVIGATION_BAR_ITEM_Charts").performClick()
        composeTestRule.waitForIdle()
    }

    fun checkLineListing(name: String) {
        onView(withId(R.id.analytics_recycler))
            .check(matches(hasDescendant(withText(name))))
            .check(matches(isDisplayed()))
    }

    fun checkChartName(itemPosition: Int, chartName: String) {
        onView(withId(R.id.analytics_recycler)).check(
            matches(
                atPosition(
                    itemPosition,
                    hasDescendant(allOf(withId(R.id.chart_title), withText(chartName))),
                ),
            ),
        )
    }

    fun clickLineListingMenu(itemPosition: Int) {
        onView(withId(R.id.analytics_recycler))
            .perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    itemPosition,
                    clickChildViewWithId(R.id.chartVisualizationButton),
                ),
            )
    }

    fun clickColumnFilter(columnName: String) {
        onView(withText(columnName)).inRoot(isPlatformPopup()).perform(click())
    }

    @OptIn(ExperimentalTestApi::class)
    fun typeColumnFilterValue(composeTestRule: ComposeTestRule, value: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasSetTextAction(), 5000L)
        composeTestRule.onNode(hasSetTextAction()).performTextInput(value)
    }

    fun clickSearchButton(composeTestRule: ComposeTestRule) {
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()
    }

    fun checkColumnContainsOnly(composeTestRule: ComposeTestRule, excludedValue: String) {
        composeTestRule.onAllNodesWithText(excludedValue).assertCountEquals(0)
    }

    fun checkFilterBadgeVisible(itemPosition: Int) {
        onView(withId(R.id.analytics_recycler)).check(
            matches(
                atPosition(
                    itemPosition,
                    hasDescendant(
                        allOf(withId(R.id.chartFilters), withEffectiveVisibility(Visibility.VISIBLE)),
                    ),
                ),
            ),
        )
    }

    fun checkFilterBadgeGone(itemPosition: Int) {
        onView(withId(R.id.analytics_recycler)).check(
            matches(
                atPosition(
                    itemPosition,
                    hasDescendant(
                        allOf(withId(R.id.chartFilters), withEffectiveVisibility(Visibility.GONE)),
                    ),
                ),
            ),
        )
    }

    fun clickReset() {
        onView(withText(R.string.reset)).inRoot(isPlatformPopup()).perform(click())
    }

    fun clickChartGroup(groupName: String) {
        onView(withText(groupName)).perform(click())
        composeTestRule.waitForIdle()
    }

    fun checkChartGroupTabDisplayed(groupName: String) {
        onView(withText(groupName)).check(matches(isDisplayed()))
    }

    fun checkChartTypeRendered(itemPosition: Int, chartType: ChartType) {
        onView(withId(R.id.analytics_recycler)).check(
            matches(
                atPosition(
                    itemPosition,
                    hasDescendant(ChartMatchers.hasChartType(chartType)),
                ),
            ),
        )
    }

    fun checkNoErrorIcon(itemPosition: Int) {
        onView(withId(R.id.analytics_recycler)).check(
            matches(
                atPosition(
                    itemPosition,
                    hasDescendant(
                        not(allOf(withId(R.id.error_data_icon), withEffectiveVisibility(Visibility.VISIBLE))),
                    ),
                ),
            ),
        )
    }
}
