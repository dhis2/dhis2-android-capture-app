package org.dhis2.usescases.teidashboard.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.atPosition
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.hamcrest.Matchers.allOf

fun indicatorsRobot(indicatorsRobot: IndicatorsRobot.() -> Unit) {
    IndicatorsRobot().apply {
        indicatorsRobot()
    }
}

class IndicatorsRobot : BaseRobot() {

    fun checkDetails(yellowFeverIndicator: String, weightIndicator: String) {
        onView(withId(R.id.indicators_recycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(
                        1,
                        hasDescendant(
                            allOf(
                                withText(yellowFeverIndicator),
                                hasSibling(
                                    allOf(
                                        withId(R.id.indicator_name),
                                        withText("Measles + Yellow fever doses")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        onView(withId(R.id.indicators_recycler)).check(
            matches(
                allOf(
                    isDisplayed(), isNotEmpty(),
                    atPosition(
                        0,
                        hasDescendant(
                            allOf(
                                withText(weightIndicator),
                                hasSibling(
                                    allOf(
                                        withId(R.id.indicator_name),
                                        withText("Average weight (g)")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
