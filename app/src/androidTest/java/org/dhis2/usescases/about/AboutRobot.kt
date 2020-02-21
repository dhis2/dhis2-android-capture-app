package org.dhis2.usescases.about

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot


class AboutRobot(context:Context) : BaseRobot(context){

    fun checkVersionNames(appName:String, sdkName:String){
        val appNameText = "App Version: $appName"
        //val sdkNameText = "SDK Version: " + sdkName
        onView(withId(R.id.aboutApp)).check(matches(withText(appNameText)))
        onView(withId(R.id.appSDK)).check(matches(withText(sdkName)))
    }
}