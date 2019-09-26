package org.dhis2.usescases.main

import android.view.Gravity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.activityScenarioRule
import org.dhis2.R
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class MainActivityTest {

    @get: Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun chekMainActivityIsVisible() {

        onView(ViewMatchers.withId(R.id.menu)).perform(ViewActions.click())

        onView(ViewMatchers.withId(R.id.drawer_layout)).check { view, noViewFoundException ->
            Assert.assertTrue((view as DrawerLayout).isDrawerOpen(Gravity.START))
        }
    }

}