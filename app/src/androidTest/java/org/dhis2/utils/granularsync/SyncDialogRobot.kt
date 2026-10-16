package org.dhis2.utils.granularsync

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import org.dhis2.R
import org.dhis2.common.BaseRobot

fun syncDialogRobot(
    composeTestRule: ComposeTestRule,
    syncDialogRobot: SyncDialogRobot.() -> Unit,
) {
    SyncDialogRobot(composeTestRule).apply {
        syncDialogRobot()
    }
}

class SyncDialogRobot(private val composeTestRule: ComposeTestRule) : BaseRobot() {

    /**
     * Asserts the granular-sync bottom sheet is showing for a record that has not
     * been uploaded yet (ANDROAPP-4836 / ANDROAPP-4837).
     *
     * `SyncStatusDialog` renders its content as `BottomSheetDialogUi`, whose title
     * comes from the record's sync state — a locally-created event is not synced, so
     * the sheet reads `sync_dialog_title_not_synced`. Asserting the visible title
     * keeps this on the UI rather than probing the FragmentManager for the dialog's
     * tag, and it proves the sheet actually rendered rather than merely that a
     * fragment was attached.
     */
    @OptIn(ExperimentalTestApi::class)
    fun checkNotSyncedDialogIsDisplayed() {
        val title = getString(R.string.sync_dialog_title_not_synced)
        composeTestRule.waitUntilAtLeastOneExists(hasText(title, substring = true), TIMEOUT)
        composeTestRule.onNodeWithText(title, substring = true).assertIsDisplayed()
    }
}
