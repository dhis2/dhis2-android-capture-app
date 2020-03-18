package org.dhis2.usescases.pin

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import org.hamcrest.CoreMatchers.allOf
import kotlinx.android.synthetic.main.layout_pin.view.*
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.usescases.main.MainActivity


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

    fun clickPinButton(button: String) {
        onView(withText(button)).perform(click())
    }

    fun checkRedirectToHome(){
        Intents.intended(allOf(hasComponent(MainActivity::class.java.name)))
    }

    fun checkToastDisplayed(text: String){
        //onView(withText(text)).inRoot(withDecorView(not(`is`(getActivity(context)!!.getWindow().getDecorView())))).check(matches(isDisplayed()))
        //onView(withText(text)).inRoot(withDecorView(not(`is`(getActivity(context)!!.getWindow().getDecorView())))).check(matches(isDisplayed()))
    }
}