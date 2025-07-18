package org.dhis2.usescases.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasParent
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
import org.dhis2.usescases.reservedValue.ReservedValueActivity

fun settingsRobot(settingsRobot: SettingsRobot.() -> Unit) {
    SettingsRobot().apply {
        settingsRobot()
    }
}

class SettingsRobot : BaseRobot() {

    fun clickOnSyncData(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(SettingItem.DATA_SYNC.name).performClick()
    }

    fun checkEditPeriodIsDisableForData(composeTestRule: ComposeTestRule) {
        composeTestRule.onNode(
            hasParent(hasTestTag(SettingItem.DATA_SYNC.name)) and
                    hasText(NOT_EDIT_TEXT)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTag_DataPeriod).assertIsNotDisplayed()
    }

    fun clickOnSyncConfiguration(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(SettingItem.META_SYNC.name).performClick()
    }

    fun checkEditPeriodIsDisableForConfiguration(composeTestRule: ComposeTestRule) {
        composeTestRule.onNode(
            hasParent(hasTestTag(SettingItem.META_SYNC.name)) and
                    hasText(NOT_EDIT_TEXT)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTag_MetaPeriod).assertIsNotDisplayed()
    }

    fun clickOnSyncParameters(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(SettingItem.SYNC_PARAMETERS.name).performClick()
    }

    fun checkEditPeriodIsDisableForParameters(composeTestRule: ComposeTestRule) {
        composeTestRule.onNode(
            hasParent(hasTestTag(SettingItem.SYNC_PARAMETERS.name)) and
                    hasText(SYNC_PARAMETERS_NOT_EDIT_TEXT)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTag_SyncParameters_LimitScope).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(TestTag_SyncParameters_EventMaxCount).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(TestTag_SyncParameters_TeiMaxCount).assertIsNotDisplayed()
    }

    fun clickOnReservedValues(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(SettingItem.RESERVED_VALUES.name).performClick()
    }

    fun clickOnManageReservedValues(composeTestRule: ComposeTestRule) {
        composeTestRule.onNode(
            hasParent(hasTestTag(SettingItem.RESERVED_VALUES.name)) and
                    hasText(getString(R.string.manage_reserved_values_button), ignoreCase = true)
        ).performClick()

        Intents.intended(IntentMatchers.hasComponent(ReservedValueActivity::class.java.name))
    }

    fun clickOnOpenSyncErrorLog(composeTestRule: ComposeTestRule) {
        composeTestRule.onNodeWithTag(SettingItem.ERROR_LOG.name).performClick()
    }

    fun checkLogViewIsDisplayed() {
        waitForView(withId(R.id.errorRecycler)).check(matches(isDisplayed()))
    }

    companion object {
        const val NOT_EDIT_TEXT = "Syncing period is not editable"
        const val SYNC_PARAMETERS_NOT_EDIT_TEXT = "Sync parameters are not editable"
        const val SYNC_DATA = "SYNC DATA NOW"
    }
}
