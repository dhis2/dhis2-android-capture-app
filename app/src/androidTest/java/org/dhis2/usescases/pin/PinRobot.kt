package org.dhis2.usescases.pin

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.android.synthetic.main.layout_pin.view.*
import org.dhis2.R
import org.dhis2.common.BaseRobot


fun pinRobot(pinBody: PinRobot.() -> Unit) {
    PinRobot().apply {
        pinBody()
    }
}

class PinRobot : BaseRobot() {

    fun checkErrorAlertIsVisible() {
        onView(withId(R.id.lockPin)).check(matches(withText("Wrong pin")))
    }

    fun clickForgotCode() {
        onView(withId(R.id.forgotCode)).perform(click())
    }
}