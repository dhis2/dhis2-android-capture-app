package org.dhis2.composetable

import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.dhis2.composetable.model.FakeModelType
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.ui.TableColors
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RowTableTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    private val tableColors = TableColors()

    var primaryColor: Color = lightColors().primary

    @Test
    fun shouldClickOnFirstRowElementAndHighlightAllElements() {
        tableRobot(composeTestRule) {
            val fakeModel = initTableAppScreen(FakeModelType.MULTIHEADER_TABLE)
            val firstTableId = fakeModel[0].id!!

            composeTestRule.waitForIdle()
            clickOnRowHeader(firstTableId, 0)
            assertRowHeaderBackgroundChangeToPrimary(
                firstTableId,
                0,
                primaryColor.let { tableColors.copy(primary = it) } ?: tableColors
            )
        }
    }

    @Test
    fun should_show_information_icon() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(FakeModelType.MULTIHEADER_TABLE)
            val firstTableId = fakeModel[0].id!!
            assertInfoIcon(firstTableId, 0)
        }
    }

    @Test
    fun should_all_rows_build_properly() {
        tableRobot(composeTestRule) {
            val fakeModel = initTable(FakeModelType.MULTIHEADER_TABLE)
            val firstTableId = fakeModel[0].id!!
            val secondTableId = fakeModel[1].id!!

            assert(fakeModel[0].tableRows.size == 3)
            assertRowHeaderText(firstTableId, "Text 1", 0)
            assertRowHeaderText(firstTableId, "Text 2", 1)
            assertRowHeaderText(firstTableId, "Text 3", 2)

            assertRowHeaderIsClickable(firstTableId, "Text 1", 0)
            assertRowHeaderIsClickable(firstTableId, "Text 2", 1)
            assertRowHeaderIsClickable(firstTableId, "Text 3", 2)

            assert(fakeModel[1].tableRows.size == 5)

            assertRowHeaderText(secondTableId, "Number", 0)
            assertRowHeaderText(secondTableId, "Text", 1)
            assertRowHeaderText(secondTableId, "Long Text", 2)
            assertRowHeaderText(secondTableId, "Integer", 3)
            assertRowHeaderText(secondTableId, "Percentage", 4)

            assertRowHeaderIsClickable(secondTableId, "Number", 0)
            assertRowHeaderIsClickable(secondTableId, "Text", 1)
            assertRowHeaderIsClickable(secondTableId, "Long Text", 2)
            assertRowHeaderIsClickable(secondTableId, "Integer", 3)
            assertRowHeaderIsClickable(secondTableId, "Percentage", 4)
        }
    }
}