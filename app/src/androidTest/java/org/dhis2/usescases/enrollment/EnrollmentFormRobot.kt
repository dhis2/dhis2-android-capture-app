package org.dhis2.usescases.enrollment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.hamcrest.CoreMatchers.equalTo


fun enrollmentFormRobot(enrollmentFormRobot: EnrollmentFormRobot.() -> Unit) {
    EnrollmentFormRobot().apply {
        enrollmentFormRobot()
    }
}

class EnrollmentFormRobot : BaseRobot() {

    fun changePickerDate() {
        onView(withId(equalTo(R.id.datePicker))).perform(PickerActions.setDate(2020, 1, 1))
    }

    fun clickOnAcceptEnrollmentDate() {
        onView(withId(R.id.acceptBtn)).perform(ViewActions.click())
    }

    fun checkDateWarningIsDisplayed() {
        onView(withText(R.string.enrollment_date_edition_warning)).check(matches(isDisplayed()))
    }
}