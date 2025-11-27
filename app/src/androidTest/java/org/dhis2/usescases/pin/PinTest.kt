package org.dhis2.usescases.pin

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinTest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun openPin() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnPin()
        }
    }

    private fun startActivity() {
        rule.launchActivity(null)
    }
}
