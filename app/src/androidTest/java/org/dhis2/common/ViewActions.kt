package org.dhis2.common

import android.view.View
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

fun openSpinnerPopup(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(Spinner::class.java)
        }

        override fun getDescription(): String {
            return "Opening Spinner"
        }

        override fun perform(uiController: UiController, view: View) {
            val spinner = view as Spinner
            spinner.performClick()
        }
    }
}
fun setSwitchCheckTo(checkValue: Boolean) : ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Setting switch check"
        }

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(SwitchCompat::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            if (view is SwitchCompat) {
                view.isChecked = checkValue
            } else {
                throw IllegalArgumentException("Error the view is not a switch")
            }
        }
    }
}
