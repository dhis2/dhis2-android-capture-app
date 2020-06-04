package org.dhis2.usescases.searchte

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.RecyclerviewMatchers
import org.dhis2.common.matchers.RecyclerviewMatchers.Companion.isNotEmpty
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTEViewHolder
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

fun searchTeiRobot(searchTeiRobot: SearchTeiRobot.() -> Unit) {
    SearchTeiRobot().apply {
        searchTeiRobot()
    }
}

class SearchTeiRobot : BaseRobot() {

    fun closeSearchForm () {
        onView(withId(R.id.close_filter)).perform(click())
    }

    fun clickOnSearchFab () {
        onView(withId(R.id.enrollmentButton)).perform(click())
    }

    fun clickOnTEI(position: Int, teiName: String) {
        /*onView(withId(R.id.scrollView)).perform(
            scrollTo<SearchTEViewHolder>(
                hasDescendant(withText(teiName))
            ),
            click()
        )*/

        onView(withId(R.id.scrollView)).perform(
            actionOnItemAtPosition<SearchTEViewHolder>(position, click())
        )
    }

    fun checkTEIsDelete(teiName: String) {
        onView(withId(R.id.scrollView)).check(
            matches(allOf(
                isDisplayed(), isNotEmpty(),
                not(hasDescendant(withText(teiName)))
            ))
        )
    }

}
