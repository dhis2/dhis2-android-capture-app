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
