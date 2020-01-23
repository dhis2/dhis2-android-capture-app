package org.dhis2.common

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice

open class BaseRobot (val context: Context){

    fun pressBack(): BaseRobot {
        UiDevice.getInstance(getInstrumentation()).pressBack()
        return this
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