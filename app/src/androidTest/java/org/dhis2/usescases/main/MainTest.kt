package org.dhis2.usescases.main

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
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
    }

    @Test
    fun checkHomeScreenRecyclerviewHasElements() {
        startActivity(
            MainActivity.intent(
                ApplicationProvider.getApplicationContext(),
                MainScreenType.Home(HomeScreen.Programs),
            )
        )
        homeRobot(composeTestRule) {
            composeTestRule.waitForIdle()
            checkViewIsNotEmpty(composeTestRule)
        }
    }

    @Test
    fun shouldNavigateToHomeWhenBackPressed() {
        startActivity(
            MainActivity.intent(
                ApplicationProvider.getApplicationContext(),
                MainScreenType.Settings,
            )
        )
        homeRobot(composeTestRule) {
            pressBack()
            checkHomeIsDisplayed(composeTestRule)
        }
    }

    private fun startActivity(intent: Intent) {
        rule.launch(intent)
    }
}
