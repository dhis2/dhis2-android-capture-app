import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.viewactions.clickChildViewWithId
import org.dhis2.ui.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.ui.dialogs.bottomsheet.TITLE
import org.dhis2.usescases.datasets.datasetDetail.datasetList.DataSetListViewHolder

fun syncFlowRobot(
    composeTestRule: ComposeTestRule,
    syncFlowRobot: SyncFlowRobot.() -> Unit) {
    SyncFlowRobot(composeTestRule).apply {
        syncFlowRobot()
    }
}

class SyncFlowRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnSyncButton() {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG, useUnmergedTree = true).performClick()
    }

    fun checkSyncWasSuccessfully() {
        val expectedTitle = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.sync_dialog_title_not_synced)
        composeTestRule.onNodeWithTag(TITLE, useUnmergedTree = true).assert(hasText(expectedTitle))
    }

    fun checkSyncFailed() {
        val expectedTitle = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.sync_dialog_title_not_synced)
        composeTestRule.onNodeWithTag(TITLE, useUnmergedTree = true).assert(hasText(expectedTitle))
    }

    @OptIn(ExperimentalTestApi::class)
    fun clickOnEventToSync() {
        composeTestRule.waitUntilAtLeastOneExists(hasText("Sync"))
        composeTestRule.onNodeWithText("Sync", useUnmergedTree = true).performClick()
    }

    fun clickOnDataSetToSync(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<DataSetListViewHolder>(
                    position,
                    clickChildViewWithId(R.id.sync_icon)
                )
            )
    }
} 