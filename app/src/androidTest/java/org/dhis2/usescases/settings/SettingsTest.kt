package org.dhis2.usescases.settings

import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.mobile.commons.featureconfig.model.Feature
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainScreenType
import org.dhis2.usescases.main.homeRobot
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SettingsState
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.usescases.settings.ui.SettingsScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

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
