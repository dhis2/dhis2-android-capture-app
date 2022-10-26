package org.dhis2.composetable.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Test

class TableDimensionsTest {

    @Test
    fun `Should calculate default cell width for different tables`() {
        val expectedExtraWidth = listOf(
            100.dp,
            210.dp / 2,
            60.dp / 3,
            60.dp / 4
        )
        listOf(
            TestScenario(extraSpacing = 100.dp, totalColumns = 1, hasTotal = false),
            TestScenario(extraSpacing = 210.dp, totalColumns = 1, hasTotal = true),
            TestScenario(extraSpacing = 60.dp, totalColumns = 3, hasTotal = false),
            TestScenario(extraSpacing = 60.dp, totalColumns = 3, hasTotal = true)
        ).forEachIndexed { index, testScenario ->
            val tableDimensions = TableDimensions()

            val tableWidth = tableDimensions.tableWidth(
                totalColumns = testScenario.totalColumns,
                hasTotal = testScenario.hasTotal
            )

            val updatedTableDimensions = tableDimensions.copy(
                totalWidth = tableWidth + testScenario.extraSpacing
            )

            val cellWidth = updatedTableDimensions.defaultCellWidthWithExtraSize(
                testScenario.totalColumns,
                testScenario.hasTotal
            )
            assertTrue(
                expectedExtraWidth[index] == cellWidth - updatedTableDimensions.defaultCellWidth
            )
        }
    }

    @Test
    fun `Should add extra spacing to cell width if table is smaller than screen`() {
        val totalColumns = 4
        val hasTotal = false

        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth + 100.dp)
        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth > updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEach { calculatedHeaderCellWidth ->
                assertTrue(calculatedHeaderCellWidth > updatedTableDimensions.defaultCellWidth)
            }
        }
    }

    @Test
    fun `Should add extra spacing to cell width if table with totals is smaller than screen`() {
        val totalColumns = 4
        val hasTotal = true
        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth + 100.dp)

        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth > updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEach { calculatedHeaderCellWidth ->
                assertTrue(calculatedHeaderCellWidth > updatedTableDimensions.defaultCellWidth)
            }
        }
    }

    @Test
    fun `Should not add extra spacing to cell width if table is bigger than screen width`() {
        val totalColumns = 4
        val hasTotal = false
        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth - 100.dp)

        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth == updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEachIndexed { index, calculatedHeaderCellWidth ->
                assertTrue(
                    calculatedHeaderCellWidth == updatedTableDimensions.headerCellWidth(
                        headerRowColumns[index],
                        totalColumns
                    )
                )
            }
        }
    }

    @Test
    fun `Should not add extra spacing to cell width if table with totals is bigger than screen`() {
        val totalColumns = 4
        val hasTotal = false
        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth - 100.dp)

        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth == updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEachIndexed { index, calculatedHeaderCellWidth ->
                assertTrue(
                    calculatedHeaderCellWidth == updatedTableDimensions.headerCellWidth(
                        headerRowColumns[index],
                        totalColumns
                    )
                )
            }
        }
    }

    private fun calculateWidth(
        tableDimensions: TableDimensions,
        totalColumns: Int,
        hasTotal: Boolean,
        vararg headerRowColumns: Int
    ): WidthCalculationResult {
        val cellWidth = tableDimensions.defaultCellWidthWithExtraSize(
            totalColumns = totalColumns,
            hasExtra = hasTotal
        )
        val rowHeaderWidth = tableDimensions.defaultRowHeaderCellWidthWithExtraSize(
            totalColumns = totalColumns,
            hasExtra = hasTotal
        )
        val headerCellWidthList = headerRowColumns.map { headerParentRowColumns ->
            tableDimensions.headerCellWidth(
                headerRowColumns = headerParentRowColumns,
                totalColumns = totalColumns,
                hasTotal = hasTotal
            )
        }

        return WidthCalculationResult(
            cellWidth,
            rowHeaderWidth,
            headerCellWidthList
        )
    }
}

private data class TestScenario(
    val extraSpacing: Dp,
    val totalColumns: Int,
    val hasTotal: Boolean
)

private data class WidthCalculationResult(
    val cellWidth: Dp,
    val rowHeaderCellWidth: Dp,
    val headerCellWidth: List<Dp>
)
