package org.dhis2.usescases.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainScreenType
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
        settingsRobot(composeTestRule) {
            clickOnSyncData()
            checkEditPeriodIsDisableForData()
        }
    }

    @Test
    fun shouldFindEditDisabledWhenClickOnSyncConfiguration() {
        startActivity()
        settingsRobot(composeTestRule) {
            clickOnSyncConfiguration()
            checkEditPeriodIsDisableForConfiguration()
        }
    }

    @Test
    fun shouldFindEditDisableWhenClickOnSyncParameters() {
        startActivity()
        settingsRobot(composeTestRule) {
            clickOnSyncParameters()
            checkEditPeriodIsDisableForParameters()
        }
    }

    @Test
    fun shouldRefillValuesWhenClickOnReservedValues() {
        startActivity()
        settingsRobot(composeTestRule) {
            clickOnReservedValues()
            clickOnManageReservedValues()
        }
    }

    @Test
    fun shouldSuccessfullyOpenLogs() {
        startActivity()
        settingsRobot(composeTestRule) {
            clickOnOpenSyncErrorLog()
            checkLogViewIsDisplayed()
        }
    }

    private fun startActivity() {
        val intent = MainActivity.intent(
            ApplicationProvider.getApplicationContext(),
            MainScreenType.Settings
        )
        rule.launch(intent)
    }
}
