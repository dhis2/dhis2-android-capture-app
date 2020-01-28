package org.dhis2.usescases.main

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import org.dhis2.R
import org.dhis2.common.BaseRobot

class MainRobot(context: Context) : BaseRobot(context){

    fun clickOnNavigationDrawerMenu() = apply {
        onView(withId(R.id.menu)).perform(click())
    }

    fun clickOnSettings() = apply {
      //  onView(withId(R.id.menu)).perform(NavigationViewActions.navigateTo(R.id.sync_manager))
    }
}