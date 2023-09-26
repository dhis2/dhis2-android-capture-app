package org.dhis2.usescases.datasets

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.utils.customviews.DateViewHolder
import org.hamcrest.CoreMatchers.allOf


fun dataSetInitialRobot(dataSetInitialRobot: DataSetInitialRobot.() -> Unit) {
    DataSetInitialRobot().apply {
        dataSetInitialRobot()
    }
}

class DataSetInitialRobot : BaseRobot() {

    fun clickOnInputOrgUnit() {
        onView(withId(R.id.dataSetOrgUnitInputLayout)).perform(click())
    }

    fun clickOnInputPeriod() {
        onView(withId(R.id.dataSetPeriodInputLayout)).perform(click())
    }

    fun clickOnActionButton() {
        onView(withId(R.id.actionButton)).perform(click())
    }

    fun selectPeriod(period: String) {
        onView(withId(R.id.recycler_date))
            .perform(RecyclerViewActions.actionOnItem<DateViewHolder>(hasDescendant(withText(period)), click()))
    }
}