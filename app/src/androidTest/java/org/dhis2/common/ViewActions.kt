package org.dhis2.common

import android.view.View
import android.widget.Spinner
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

class ViewActions {
    companion object {
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
    }
}