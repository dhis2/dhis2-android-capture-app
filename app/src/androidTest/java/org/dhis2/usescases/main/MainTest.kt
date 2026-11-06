package org.dhis2.usescases.main

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<MainActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun shouldNavigateToHomeWhenBackPressed() {
        startActivity(
            MainActivity.intent(
                ApplicationProvider.getApplicationContext(),
                MainScreenType.Settings,
            )
        )
        homeRobot(composeTestRule) {
            pressBack()
            checkHomeIsDisplayed(composeTestRule)
        }
    }

    @Test
    fun shouldShowHomeVisualizations() {
        startActivity(
            MainActivity.intent(
                ApplicationProvider.getApplicationContext(),
                MainScreenType.Home(HomeScreen.Programs),
            )
        )
        homeRobot(composeTestRule) {
            checkViewIsNotEmpty(composeTestRule)
            // [ANDROAPP-4356] Visualizations - Location: the charts/analytics tab is present
            clickChartsTab()
            // [ANDROAPP-4605] Visualizations - Group name: the "Line listing" group tab is labelled
            checkChartGroupTabDisplayed("Line listing")
            // [ANDROAPP-5964] Line Listing - Rendering: the line listing loads and is displayed
            checkLineListing("Automated Test: Line Listing")
            // [ANDROAPP-4606] Visualizations - Chart name: the line listing title is displayed
            checkChartName(0, "Automated Test: Line Listing")
            // [ANDROAPP-5964] Line Listing - Rendering: the line listing shows enrollment rows
            checkChartTypeRendered(0, ChartType.LINE_LISTING)
            clickLineListingMenu(0)
            // [ANDROAPP-5967] Line Listing - custom filter (search): filter a column by a typed value
            clickColumnFilter("Gender")
            typeColumnFilterValue(composeTestRule, "Female")
            clickSearchButton(composeTestRule)
            // [ANDROAPP-5966] Line Listing - filters: only matching rows remain after filtering
            checkColumnContainsOnly(composeTestRule, "Male")
            checkTableHasValue(0, "Female")
            // [ANDROAPP-5966] Line Listing - filters: active-filter badge is shown
            checkFilterBadgeVisible(0)
            // [ANDROAPP-4363] Visualizations - Filter - Reset: clear the filter and badge disappears
            clickLineListingMenu(0)
            clickReset()
            checkFilterBadgeGone(0)

            // [ANDROAPP-4661] Visualizations - Type: Radar
            clickChartGroup("Radar")
            checkChartName(0, "Android: ANC visits (radar)")
            checkChartTypeRendered(0, ChartType.RADAR)
            // [ANDROAPP-7914] Visualizations - Data: every radar in the group plots values
            checkChartHasData(0, ANC_RADAR_ORG_UNIT_COUNT)
            checkNoErrorIcon(0)
            scrollToChart(1)
            checkChartName(1, "Visits by month")
            checkChartTypeRendered(1, ChartType.RADAR)
            checkChartHasData(1, ANC_RADAR_MONTH_COUNT)
            checkNoErrorIcon(1)

            // [ANDROAPP-4662] Visualizations - Type: Pie
            clickChartGroup("Pie chart")
            checkChartName(0, "Android: Malaria case count pie")
            checkChartTypeRendered(0, ChartType.PIE_CHART)
            // [ANDROAPP-7914] Visualizations - Data: the pie plots a slice per org unit
            checkChartHasData(0, MALARIA_PIE_SLICE_COUNT)
            checkNoErrorIcon(0)

            // [ANDROAPP-7714] PI disaggregation - Rendering: open the "PI disaggregation" group
            checkChartGroupTabDisplayed("PI disaggregation")
            clickChartGroup("PI disaggregation")
            for (position in 0 until PI_DISAGGREGATION_VISUALIZATION_COUNT) {
                scrollToChart(position)
                checkChartTypeRendered(position, ChartType.TABLE)
                // [ANDROAPP-7914] PI disaggregation - Values: no table cell is left empty
                checkTableHasNoEmptyValues(position)
                checkNoErrorIcon(position)
            }
        }
    }

    private fun startActivity(intent: Intent) {
        rule.launch(intent)
    }

    private companion object {
        const val PI_DISAGGREGATION_VISUALIZATION_COUNT = 13

        // Axes and slices of the home charts: one per org unit or month in their layout
        const val ANC_RADAR_ORG_UNIT_COUNT = 5
        const val ANC_RADAR_MONTH_COUNT = 6
        const val MALARIA_PIE_SLICE_COUNT = 5
    }
}
