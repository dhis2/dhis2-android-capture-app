package org.dhis2.composetable

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.dhis2.composetable.activity.TableTestActivity
import org.dhis2.composetable.model.FakeTableModels
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.TableColors
import org.junit.Rule
import org.junit.Test

class ColumnTableTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TableTestActivity>()
    private val tableColors = TableColors()

    @Test
    fun shouldAllColumnsBuildProperly(){
        val fakeModel = FakeTableModels(
            context = composeTestRule.activity.applicationContext
        ).getMultiHeaderTables()

        initTable(fakeModel)

        val firstTableId = fakeModel[0].id!!
        val secondTableId = fakeModel[1].id!!

        tableRobot(composeTestRule) {
           // assert(fakeModel[0].tableHeaderModel.rows.


        /*    assertRowHeaderText(firstTableId, "Text 1", 0)
            assertRowHeaderText(firstTableId, "Text 2", 1)
            assertRowHeaderText(firstTableId, "Text 3", 2)

            assertRowHeaderIsClickable(firstTableId, "Text 1",0)
            assertRowHeaderIsClickable(firstTableId, "Text 2",1)
            assertRowHeaderIsClickable(firstTableId, "Text 3",2) */
        }
    }

    private fun initTable(fakeModel: List<TableModel>) {
        composeTestRule.setContent {
            DataTable(
                tableList = fakeModel,
                tableColors = tableColors,
                onDecorationClick = {

                },
                onClick = {

                }
            )
        }
    }

}