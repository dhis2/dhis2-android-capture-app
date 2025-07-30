package org.dhis2.usescases.settings

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.AVOID_SYNC
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseTest() {

    @get:Rule
    val rule = lazyActivityScenarioRule<MainActivity>(launchActivity = false)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    override fun setUp() {
        super.setUp()
        enableIntents()
    }

    @Test
    fun shouldFindEditPeriodDisabledWhenClickOnSyncData() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSyncData(composeTestRule)
            checkEditPeriodIsDisableForData(composeTestRule)
        }
    }

    @Test
    fun shouldFindEditDisabledWhenClickOnSyncConfiguration() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSyncConfiguration(composeTestRule)
            checkEditPeriodIsDisableForConfiguration(composeTestRule)
        }
    }

    @Test
    fun shouldFindEditDisableWhenClickOnSyncParameters() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnSyncParameters(composeTestRule)
            checkEditPeriodIsDisableForParameters(composeTestRule)
        }
    }

    @Test
    fun shouldRefillValuesWhenClickOnReservedValues() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnReservedValues(composeTestRule)
            clickOnManageReservedValues(composeTestRule)
        }
    }

    @Test
    fun shouldSuccessfullyOpenLogs() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickOnSettings()
        }

        settingsRobot {
            clickOnOpenSyncErrorLog(composeTestRule)
            checkLogViewIsDisplayed()
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
