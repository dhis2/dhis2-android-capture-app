package org.dhis2.composetable

import androidx.compose.ui.unit.dp
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class TableHeaderTest {

    val tableHeaderModel = TableHeader(
        rows = listOf(
            TableHeaderRow(
                cells = listOf(
                    TableHeaderCell(value = "<18"),
                    TableHeaderCell(value = ">18 <65"),
                    TableHeaderCell(value = ">65")
                )
            ),
            TableHeaderRow(
                cells = listOf(
                    TableHeaderCell(value = "Male"),
                    TableHeaderCell(value = "Female")
                )
            ),
            TableHeaderRow(
                cells = listOf(
                    TableHeaderCell(value = "Fixed"),
                    TableHeaderCell(value = "Outreach")
                )
            )
        )
    )

    @Test
    fun numberOfCellsInHeaderRow() {
        assertTrue(tableHeaderModel.numberOfColumns(0) == 3)
        assertTrue(tableHeaderModel.numberOfColumns(1) == 6)
        assertTrue(tableHeaderModel.numberOfColumns(2) == 12)
    }

    @Ignore("Indeterministic")
    @Test
    fun widthCellInHeaderRow() {
        assertTrue(tableHeaderModel.headerCellWidth(0) == 200.dp)
        assertTrue(tableHeaderModel.headerCellWidth(1) == 100.dp)
        assertTrue(tableHeaderModel.headerCellWidth(2) == 50.dp)
    }
}
