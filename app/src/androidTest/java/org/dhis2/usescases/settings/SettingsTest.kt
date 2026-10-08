package org.dhis2.usescases.settings

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.dhis2.R
import org.dhis2.lazyActivityScenarioRule
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainScreenType
import org.dhis2.usescases.main.homeRobot
import org.dhis2.usescases.settings.ui.TwoFASettingItem
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.junit.Assert.assertTrue
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
     * The test account is BASIC, so it also checks the 2FA entry point is hidden (ANDROAPP-7232).
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

            // 2FA entry point is only offered to OAuth accounts (ANDROAPP-7232)
            checkTwoFAOptionIsNotShownForBasicAccount()

            // Back from Settings returns to Home (former shouldNavigateToHomeWhenBackPressed)
            pressBack()
        }
        homeRobot(composeTestRule) {
            checkHomeIsDisplayed(composeTestRule)
        }
    }

    /**
     * 2FA settings menu item (ANDROAPP-7232). The item is only rendered for OAuth accounts and the
     * test database logs in with a BASIC one, so the item is rendered in isolation instead of
     * launching MainActivity. Content is set once and the status is switched through state, so
     * every status is checked without paying the per-test setup again.
     */
    @Test
    fun shouldShowTwoFASettingItemForEachStatus() {
        val status = mutableStateOf<TwoFAStatus>(TwoFAStatus.Enabled())
        var clicked = false
        composeTestRule.setContent {
            DHIS2Theme {
                TwoFASettingItem(
                    status = status.value,
                    onClick = { clicked = true },
                )
            }
        }
        settingsRobot(composeTestRule) {
            // Enabled: status and description are shown
            checkTwoFAOptionIsDisplayed()
            checkTwoFAStatusIs(getString(R.string.settingsTwoFAEnabled))

            // Disabled: status and description are shown
            status.value = TwoFAStatus.Disabled(secretCode = "SECRET")
            checkTwoFAStatusIs(getString(R.string.settingsTwoFADisabled))

            // No connection: only the title is shown
            status.value = TwoFAStatus.NoConnection
            checkTwoFAStatusIsHidden()

            // Clicking the item triggers the open 2FA settings action
            clickOnTwoFASettings()
        }
        assertTrue(clicked)
    }

    private fun startActivity() {
        val intent = MainActivity.intent(
            ApplicationProvider.getApplicationContext(),
            MainScreenType.Settings
        )
        rule.launch(intent)
    }
}
