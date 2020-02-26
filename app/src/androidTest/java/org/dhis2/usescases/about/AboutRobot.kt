package org.dhis2.usescases.about

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun aboutRobot(aboutBody: AboutRobot.() -> Unit){
    AboutRobot().run {
        aboutBody
    }
}

class AboutRobot : BaseRobot(){

    fun checkVersionNames(appName:String, sdkName:String){
        val appNameText = "App Version: $appName"
        onView(withId(R.id.aboutApp)).check(matches(withText(appNameText)))
        onView(withId(R.id.appSDK)).check(matches(withText(sdkName)))
    }
}