package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.hasItem

fun indicatorsRobot(
    composeTestRule: ComposeTestRule,
    indicatorsRobot: IndicatorsRobot.() -> Unit
) {
    IndicatorsRobot(composeTestRule).apply {
        indicatorsRobot()
    }
}

class IndicatorsRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    @OptIn(ExperimentalTestApi::class)
    fun checkDetails(yellowFeverIndicator: String, weightIndicator: String) {
        composeTestRule.waitUntilAtLeastOneExists(hasText(yellowFeverIndicator),TIMEOUT)
        composeTestRule.onNodeWithText(yellowFeverIndicator).assertIsDisplayed()
        composeTestRule.waitUntilAtLeastOneExists(hasText(weightIndicator),TIMEOUT)
        composeTestRule.onNodeWithText(weightIndicator).assertIsDisplayed()
    }

    fun checkGraphIsRendered(chartName: String) {
        waitForView(withId(R.id.indicators_recycler)).check(
            matches(
                atPosition(
                    1,
                    hasDescendant(withText(chartName))
                )
            )
        )
    }
}
