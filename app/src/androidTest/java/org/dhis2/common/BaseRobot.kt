package org.dhis2.common

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import org.dhis2.R
import org.hamcrest.CoreMatchers.allOf

open class BaseRobot {

    fun pressBack(): BaseRobot {
        UiDevice.getInstance(getInstrumentation()).pressBack()
        return this
    }

    fun acceptGenericDialog(){
        onView(withId(R.id.dialogAccept)).perform(ViewActions.click())
    }

    fun closeKeyboard() {
        onView(isRoot()).perform(closeSoftKeyboard())
    }

    fun pressImeActionButton(@IdRes editTextId: Int? = null) {
        if (editTextId != null) {
            onView(withId(editTextId)).perform(ViewActions.pressImeActionButton())
        } else {
            onView(allOf(isAssignableFrom(EditText::class.java), hasFocus())).perform(ViewActions.pressImeActionButton())
        }
    }

    inline fun <reified T : Activity> waitUntilActivityVisible() {
        val startTime = System.currentTimeMillis()
        while (!isVisible<T>()) {
            Thread.sleep(CONDITION_CHECK_INTERVAL)
            if (System.currentTimeMillis() - startTime >= TIMEOUT) {
                throw AssertionError("Activity ${T::class.java.simpleName} not visible after $TIMEOUT milliseconds")
            }
        }
    }

    inline fun <reified T : Activity> isVisible() : Boolean {
        val am = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val visibleActivityName = am.appTasks[0].taskInfo.baseActivity.className
        return visibleActivityName == T::class.java.name
    }

    companion object{
        const val TIMEOUT = 5000L
        const val CONDITION_CHECK_INTERVAL = 200L
    }
}