package org.dhis2.usescases.datasets

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.composetable.ui.CELL_TEST_TAG
import org.dhis2.composetable.ui.CellSelected
import org.dhis2.composetable.ui.INPUT_TEST_FIELD_TEST_TAG
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.junit.Assert.assertTrue

fun dataSetTableRobot(
    composeTestRule: ComposeContentTestRule,
    dataSetTableRobot: DataSetTableRobot.() -> Unit
) {
    DataSetTableRobot(composeTestRule).apply {
        dataSetTableRobot()
    }
}

class DataSetTableRobot(
    private val composeTestRule: ComposeContentTestRule
) : BaseRobot() {

    fun clickOnSaveButton() {
        waitForView(withId(R.id.saveButton)).perform(click())
    }

    fun clickOnPositiveButton() {
        onView(withId(R.id.positive)).perform(click())
    }

    fun clickOnNegativeButton() {
        onView(withId(R.id.negative)).perform(click())
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnMenuReOpen() {
        onView(withText(R.string.re_open)).perform(click())
    }

    fun typeOnCell(tableId: String, rowIndex: Int, columnIndex: Int) {
        composeTestRule.onNodeWithTag("$tableId$CELL_TEST_TAG$rowIndex$columnIndex", true)
            .performScrollTo()
        composeTestRule.onNodeWithTag("$tableId$CELL_TEST_TAG$rowIndex$columnIndex", true)
            .performClick()
    }

    fun assertCellSelected(tableId: String, rowIndex: Int, columnIndex: Int){
        composeTestRule.onNode(
            hasTestTag("$tableId${CELL_TEST_TAG}$rowIndex$columnIndex")
                    and
                    SemanticsMatcher.expectValue(CellSelected, true), true
        ).assertIsDisplayed()
    }

    fun clickOnEditValue() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performClick()
    }

    fun typeInput(text: String) {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performTextInput(text)
    }

    fun clickOnAccept() {
        composeTestRule.onNodeWithTag(INPUT_TEST_FIELD_TEST_TAG).performImeAction()
    }

    fun checkActivityHasNotFinished(activity: DataSetTableActivity) {
        assertTrue(!activity.isDestroyed)
    }

    fun clickOnAcceptDate() {
        onView(withText(R.string.action_accept)).perform(click())
    }
}
