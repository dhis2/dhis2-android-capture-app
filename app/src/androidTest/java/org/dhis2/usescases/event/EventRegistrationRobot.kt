package org.dhis2.usescases.event

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage
import org.dhis2.usescases.event.entity.EventDetailsUIModel
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not

fun eventRegistrationRobot(eventRegistrationRobot: EventRegistrationRobot.() -> Unit) {
    EventRegistrationRobot().apply {
        eventRegistrationRobot()
    }
}

class EventRegistrationRobot : BaseRobot() {

    fun checkEventFormDetails(eventDetails: EventDetailsUIModel) {
        onView(withId(R.id.programStageName)).check(matches(withText(eventDetails.programStage)))
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(eventDetails.completedPercentage)))
        onView(withId(R.id.eventSecundaryInfo)).check(
            matches(
                allOf(
                    withSubstring(eventDetails.eventDate),
                    withSubstring(eventDetails.orgUnit)
                )
            )
        )
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        onView(withText(R.string.delete)).perform(click())
    }

    fun clickOnDetails() {
        onView(withId(R.id.navigation_details)).perform(click())
    }

    fun checkEventDetails(eventDetails: EventDetailsUIModel) {
        onView(withId(R.id.detailsStageName)).check(matches(withText(eventDetails.programStage)))
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(eventDetails.completedPercentage)))
        onView(withId(R.id.date_layout)).check(
            matches(
                allOf(
                    isEnabled(),
                    hasDescendant(allOf(withId(R.id.date), withText(eventDetails.eventDate)))
                )
            )
        )
        onView(withId(R.id.org_unit_layout)).check(
            matches(
                allOf(
                    not(isEnabled()),
                    hasDescendant(allOf(withId(R.id.org_unit), withText(eventDetails.orgUnit)))
                )
            )
        )
    }

    fun clickOnShare() {
        onView(withText(R.string.share)).perform(click())
    }

    private fun clickOnNextQR() {
        onView(withId(R.id.next)).perform(click())
    }

    fun clickOnAllQR(listQR: Int) {
        var qrLength = 1

        while (qrLength < listQR) {
            clickOnNextQR()
            qrLength++
        }
    }

    fun clickOnDeleteDialog() {
        onView(withId(R.id.possitive)).perform(click())
    }

    fun clickLocationButton() {
        onView(withId(R.id.location1)).perform(click())
    }

    fun selectOrgUnit(orgUnitName: String) {
        onView(withId(R.id.org_unit)).perform(click())
        onView(withText(orgUnitName)).perform(click())
    }

    fun clickNextButton() {
        waitForView(withId(R.id.action_button)).perform(click())
    }
}