import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
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
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewHolder

fun syncFlowRobot(syncFlowRobot: SyncFlowRobot.() -> Unit) {
    SyncFlowRobot().apply {
        syncFlowRobot()
    }
}

class SyncFlowRobot : BaseRobot() {

    fun clickOnSyncButton(composeTestRule: ComposeContentTestRule) {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
    }

    fun checkSyncWasSuccessfully(composeTestRule: ComposeContentTestRule) {
        val expectedTitle = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.sync_dialog_title_not_synced)
        composeTestRule.onNodeWithTag(TITLE).assert(hasText(expectedTitle))
    }

    fun checkSyncFailed(composeTestRule: ComposeContentTestRule) {
        val expectedTitle = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.sync_dialog_title_not_synced)
        composeTestRule.onNodeWithTag(TITLE).assert(hasText(expectedTitle))
    }

    fun clickOnEventToSync(position: Int) {
        onView(withId(R.id.recycler))
            .perform(
                actionOnItemAtPosition<EventViewHolder>(
                    position,
                    clickChildViewWithId(R.id.sync_icon)
                )
            )
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