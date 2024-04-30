package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.assertIsDisplayed
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

fun indicatorsRobot(
    composeTestRule: ComposeTestRule,
    indicatorsRobot: IndicatorsRobot.() -> Unit
) {
    IndicatorsRobot(composeTestRule).apply {
        indicatorsRobot()
    }
}

class IndicatorsRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun checkDetails(yellowFeverIndicator: String, weightIndicator: String) {
        composeTestRule.onNodeWithText(yellowFeverIndicator).assertIsDisplayed()
        composeTestRule.onNodeWithText(weightIndicator).assertIsDisplayed()
    }

    fun checkGraphIsRendered(chartName: String) {
        onView(withId(R.id.indicators_recycler)).check(
            matches(
                atPosition(
                    1,
                    hasDescendant(withText(chartName))
                )
            )
        )
    }
}
