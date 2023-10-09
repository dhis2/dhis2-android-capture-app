package org.dhis2.composetable.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class TableDimensionsTest {

    @Test
    fun `Should calculate default cell width for different tables`() {
        val expectedExtraWidth = listOf(
            100,
            210 / 2,
            60 / 3,
            60 / 4,
        )
        listOf(
            TestScenario(extraSpacing = 100, totalColumns = 1, hasTotal = false),
            TestScenario(extraSpacing = 210, totalColumns = 1, hasTotal = true),
            TestScenario(extraSpacing = 60, totalColumns = 3, hasTotal = false),
            TestScenario(extraSpacing = 60, totalColumns = 3, hasTotal = true),
        ).forEachIndexed { index, testScenario ->
            val tableDimensions = TableDimensions()

            val tableWidth = tableDimensions.tableWidth(
                tableId = "tableId",
                totalColumns = testScenario.totalColumns,
                hasTotal = testScenario.hasTotal,
            )

            val updatedTableDimensions = tableDimensions.copy(
                totalWidth = tableWidth + testScenario.extraSpacing,
            )

            val cellWidth = updatedTableDimensions.defaultCellWidthWithExtraSize(
                tableId = "tableId",
                totalColumns = testScenario.totalColumns,
                hasExtra = testScenario.hasTotal,
            )
            assertTrue(
                expectedExtraWidth[index] == cellWidth - updatedTableDimensions.defaultCellWidth,
            )
        }
    }

    @Test
    fun `Should add extra spacing to cell width if table is smaller than screen`() {
        val tableId = "tableId"
        val totalColumns = 4
        val hasTotal = false

        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(
                tableId = tableId,
                totalColumns = totalColumns,
                hasTotal = hasTotal,
            )
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth + 100)
        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns,
        ).let { result ->
            assertTrue(result.cellWidth > updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEach { calculatedHeaderCellWidth ->
                assertTrue(calculatedHeaderCellWidth >= updatedTableDimensions.defaultCellWidth)
            }
        }
    }

    @Test
    fun `Should add extra spacing to cell width if table with totals is smaller than screen`() {
        val tableId = "tableId"
        val totalColumns = 4
        val hasTotal = true
        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(
                tableId = tableId,
                totalColumns = totalColumns,
                hasTotal = hasTotal,
            )
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth + 100)

        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns,
        ).let { result ->
            assertTrue(result.cellWidth > updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEach { calculatedHeaderCellWidth ->
                assertTrue(calculatedHeaderCellWidth >= updatedTableDimensions.defaultCellWidth)
            }
        }
    }

    @Test
    fun `Should not add extra spacing to cell width if table is bigger than screen width`() {
        val tableId = "tableId"
        val totalColumns = 4
        val hasTotal = false
        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(
                tableId = tableId,
                totalColumns = totalColumns,
                hasTotal = hasTotal,
            )
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth - 100)

        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns,
        ).let { result ->
            assertTrue(result.cellWidth == updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEachIndexed { index, calculatedHeaderCellWidth ->
                assertTrue(
                    calculatedHeaderCellWidth == updatedTableDimensions.headerCellWidth(
                        headerRowColumns[index],
                        totalColumns,
                    ),
                )
            }
        }
    }

    @Test
    fun `Should not add extra spacing to cell width if table with totals is bigger than screen`() {
        val tableId = "tableId"
        val totalColumns = 4
        val hasTotal = false
        val tableDimensions = TableDimensions()
        val tableWidth =
            tableDimensions.tableWidth(
                tableId = tableId,
                totalColumns = totalColumns,
                hasTotal = hasTotal,
            )
        val headerRowColumns = intArrayOf(2, totalColumns)

        val updatedTableDimensions = tableDimensions.copy(totalWidth = tableWidth - 100)

        calculateWidth(
            tableDimensions = updatedTableDimensions,
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns,
        ).let { result ->
            assertTrue(result.cellWidth == updatedTableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == updatedTableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEachIndexed { index, calculatedHeaderCellWidth ->
                assertTrue(
                    calculatedHeaderCellWidth == updatedTableDimensions.headerCellWidth(
                        headerRowColumns[index],
                        totalColumns,
                    ),
                )
            }
        }
    }

    private fun calculateWidth(
        tableDimensions: TableDimensions,
        totalColumns: Int,
        hasTotal: Boolean,
        vararg headerRowColumns: Int,
    ): WidthCalculationResult {
        val cellWidth = tableDimensions.defaultCellWidthWithExtraSize(
            tableId = "tableId",
            totalColumns = totalColumns,
            hasExtra = hasTotal,
        )
        val rowHeaderWidth = tableDimensions.defaultRowHeaderWidth
        val headerCellWidthList = headerRowColumns.map { headerParentRowColumns ->
            tableDimensions.headerCellWidth(
                headerRowColumns = headerParentRowColumns,
                totalColumns = totalColumns,
            )
        }

        return WidthCalculationResult(
            cellWidth,
            rowHeaderWidth,
            headerCellWidthList,
        )
    }
}

private data class TestScenario(
    val extraSpacing: Int,
    val totalColumns: Int,
    val hasTotal: Boolean,
)

private data class WidthCalculationResult(
    val cellWidth: Int,
    val rowHeaderCellWidth: Int,
    val headerCellWidth: List<Int>,
)
