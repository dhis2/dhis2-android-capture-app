package org.dhis2.usescases.main

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<MainActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        startActivity()
    }

    @Test
    fun checkHomeScreenRecyclerviewHasElements() {
        homeRobot {
            composeTestRule.waitForIdle()
            checkViewIsNotEmpty(composeTestRule)
        }
    }

    @Test
    fun shouldNavigateToHomeWhenBackPressed() {
        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
            pressBack()
            checkHomeIsDisplayed(composeTestRule)
        }
    }

    private fun startActivity() {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            MainActivity::class.java
        ).putExtra(AVOID_SYNC, true)
        rule.launch(intent)
    }
}
