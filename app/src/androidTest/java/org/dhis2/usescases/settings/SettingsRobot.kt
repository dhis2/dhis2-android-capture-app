package org.dhis2.usescases.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.mobile.login.authentication.TwoFASettingsActivity
import org.dhis2.usescases.reservedValue.ReservedValueActivity
import org.dhis2.usescases.settings.ui.TEST_TAG_DATA_PERIOD
import org.dhis2.usescases.settings.ui.TEST_TAG_META_PERIOD
import org.dhis2.usescases.settings.ui.TEST_TAG_SYNC_PARAMETERS_EVENT_MAX_COUNT
import org.dhis2.usescases.settings.ui.TEST_TAG_SYNC_PARAMETERS_LIMIT_SCOPE
import org.dhis2.usescases.settings.ui.TEST_TAG_SYNC_PARAMETERS_TEI_MAX_COUNT

fun settingsRobot(
    composeTestRule: ComposeTestRule,
    settingsRobot: SettingsRobot.() -> Unit
) {
    SettingsRobot(composeTestRule).apply {
        settingsRobot()
    }
}

class SettingsRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnSyncData() {
        composeTestRule.onNodeWithTag(SettingItem.DATA_SYNC.name).performClick()
    }

    fun checkEditPeriodIsDisableForData() {
        composeTestRule.onNodeWithTag(SettingItem.DATA_SYNC.name)
            .assertIsDisplayed()
        composeTestRule.onNode(
                    hasText(NOT_EDIT_TEXT)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_DATA_PERIOD).assertIsNotDisplayed()
    }

    fun clickOnSyncConfiguration() {
        composeTestRule.onNodeWithTag(SettingItem.META_SYNC.name).performClick()
    }

    fun checkEditPeriodIsDisableForConfiguration() {
        composeTestRule.onNodeWithTag(SettingItem.META_SYNC.name)
            .assertIsDisplayed()
        composeTestRule.onNode(
                    hasText(NOT_EDIT_TEXT)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_META_PERIOD).assertIsNotDisplayed()
    }

    fun clickOnSyncParameters() {
        composeTestRule.onNodeWithTag(SettingItem.SYNC_PARAMETERS.name).performClick()
    }

    fun checkEditPeriodIsDisableForParameters() {
        composeTestRule.onNodeWithTag(SettingItem.SYNC_PARAMETERS.name)
            .assertIsDisplayed()
        composeTestRule.onNode(
                    hasText(SYNC_PARAMETERS_NOT_EDIT_TEXT)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_SYNC_PARAMETERS_LIMIT_SCOPE).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_SYNC_PARAMETERS_EVENT_MAX_COUNT).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_SYNC_PARAMETERS_TEI_MAX_COUNT).assertIsNotDisplayed()
    }

    fun clickOnReservedValues() {
        composeTestRule.onNodeWithTag(SettingItem.RESERVED_VALUES.name).performClick()
    }

    fun clickOnManageReservedValues() {
        composeTestRule.onNode(
                    hasText(getString(R.string.manage_reserved_values_button), ignoreCase = true)
        ).performClick()

        Intents.intended(IntentMatchers.hasComponent(ReservedValueActivity::class.java.name))
    }

    fun clickOnOpenSyncErrorLog() {
        composeTestRule.onNodeWithTag(SettingItem.ERROR_LOG.name).performClick()
    }

    fun checkLogViewIsDisplayed() {
        waitForView(withId(R.id.errorRecycler)).check(matches(isDisplayed()))
    }

    fun checkTwoFAOptionIsDisplayed() {
        composeTestRule.onNodeWithTag(SettingItem.TWO_FACTOR_AUTH.name).assertIsDisplayed()
    }

    fun clickOnTwoFASettings() {
        composeTestRule.onNodeWithTag(SettingItem.TWO_FACTOR_AUTH.name).performClick()
    }

    fun checkTwoFAOptionIsNotDisplayed() {
        composeTestRule.onNodeWithTag(SettingItem.TWO_FACTOR_AUTH.name).assertIsNotDisplayed()
    }

    fun checkTwoFAScreenIsDisplayed() {
        Intents.intended(IntentMatchers.hasComponent(TwoFASettingsActivity::class.java.name))
    }

    companion object {
        const val NOT_EDIT_TEXT = "Syncing period is not editable"
        const val SYNC_PARAMETERS_NOT_EDIT_TEXT = "Sync parameters are not editable"
    }
}
