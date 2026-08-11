package org.dhis2.usescases.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainScreenType
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

    /**
     * Single workflow walking the Settings screen, then navigating back to Home.
     * Absorbs the former standalone tests: shouldFindEditPeriodDisabledWhenClickOnSyncData,
     * shouldFindEditDisabledWhenClickOnSyncConfiguration, shouldFindEditDisableWhenClickOnSyncParameters,
     * shouldRefillValuesWhenClickOnReservedValues, shouldSuccessfullyOpenLogs, and
     * MainTest.shouldNavigateToHomeWhenBackPressed (final back-to-home checkpoint).
     * The settings sections are an exclusive accordion (opening one closes the previous),
     * so each section can be opened in sequence without manual collapsing.
     */
    @Test
    fun shouldExerciseSettingsScreen() {
        startActivity()
        settingsRobot(composeTestRule) {
            // Sync Data section: syncing period is read-only
            clickOnSyncData()
            checkEditPeriodIsDisableForData()

            // Sync Configuration section: syncing period is read-only
            clickOnSyncConfiguration()
            checkEditPeriodIsDisableForConfiguration()

            // Sync Parameters section: parameters are read-only
            clickOnSyncParameters()
            checkEditPeriodIsDisableForParameters()

            // Error log opens the ErrorDialog; dismiss it to return to Settings
            clickOnOpenSyncErrorLog()
            checkLogViewIsDisplayed()
            pressBack()

            // Reserved values: "Manage" launches ReservedValueActivity; back returns to Settings
            clickOnReservedValues()
            clickOnManageReservedValues()
            pressBack()

            // Back from Settings returns to Home (former shouldNavigateToHomeWhenBackPressed)
            pressBack()
        }
        homeRobot(composeTestRule) {
            checkHomeIsDisplayed(composeTestRule)
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
