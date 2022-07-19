package org.dhis2.composetable.ui

import android.content.res.Configuration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Test

class TableDimensionsTest {

    private val tableDimensions = TableDimensions()
    private val configuration: Configuration = Configuration()

    @Test
    fun `Should calculate default cell width for different tables`() {
        val expectedExtraWidth = listOf(
            100.dp / 2,
            210.dp / 3,
            50.dp / 4,
            50.dp / 5
        )
        listOf(
            TestScenario(extraSpacing = 100.dp, totalColumns = 1, hasTotal = false),
            TestScenario(extraSpacing = 210.dp, totalColumns = 1, hasTotal = true),
            TestScenario(extraSpacing = 50.dp, totalColumns = 3, hasTotal = false),
            TestScenario(extraSpacing = 50.dp, totalColumns = 3, hasTotal = true)
        ).forEachIndexed { index, testScenario ->
            val tableWidth = tableDimensions.tableWidth(
                totalColumns = testScenario.totalColumns,
                hasTotal = testScenario.hasTotal
            )
            configuration.screenWidthDp = (tableWidth + testScenario.extraSpacing).value.toInt()

            val cellWidth = tableDimensions.defaultCellWidthWithExtraSize(
                configuration,
                testScenario.totalColumns,
                testScenario.hasTotal
            )
            assertTrue(
                expectedExtraWidth[index] == cellWidth - tableDimensions.defaultCellWidth
            )
        }
    }

    @Test
    fun `Should add extra spacing to cell width if table is smaller than screen`() {
        val totalColumns = 4
        val hasTotal = false

        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        configuration.screenWidthDp = (tableWidth + 100.dp).value.toInt()
        calculateWidth(
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth > tableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth > tableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEach { calculatedHeaderCellWidth ->
                assertTrue(calculatedHeaderCellWidth > tableDimensions.defaultCellWidth)
            }
        }
    }

    @Test
    fun `Should add extra spacing to cell width if table with totals is smaller than screen`() {
        val totalColumns = 4
        val hasTotal = true

        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        configuration.screenWidthDp = (tableWidth + 100.dp).value.toInt()
        calculateWidth(
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth > tableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth > tableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEach { calculatedHeaderCellWidth ->
                assertTrue(calculatedHeaderCellWidth > tableDimensions.defaultCellWidth)
            }
        }
    }

    @Test
    fun `Should not add extra spacing to cell width if table is bigger than screen width`() {
        val totalColumns = 4
        val hasTotal = false

        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        configuration.screenWidthDp = (tableWidth - 100.dp).value.toInt()
        calculateWidth(
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth == tableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == tableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEachIndexed { index, calculatedHeaderCellWidth ->
                assertTrue(
                    calculatedHeaderCellWidth == tableDimensions.headerCellWidth(
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

        val tableWidth =
            tableDimensions.tableWidth(totalColumns = totalColumns, hasTotal = hasTotal)
        val headerRowColumns = intArrayOf(2, totalColumns)

        configuration.screenWidthDp = (tableWidth - 100.dp).value.toInt()
        calculateWidth(
            totalColumns = totalColumns,
            hasTotal = false,
            headerRowColumns = headerRowColumns
        ).let { result ->
            assertTrue(result.cellWidth == tableDimensions.defaultCellWidth)
            assertTrue(result.rowHeaderCellWidth == tableDimensions.defaultRowHeaderWidth)
            result.headerCellWidth.forEachIndexed { index, calculatedHeaderCellWidth ->
                assertTrue(
                    calculatedHeaderCellWidth == tableDimensions.headerCellWidth(
                        headerRowColumns[index],
                        totalColumns
                    )
                )
            }
        }
    }

    private fun calculateWidth(
        totalColumns: Int,
        hasTotal: Boolean,
        vararg headerRowColumns: Int
    ): WidthCalculationResult {
        val cellWidth = tableDimensions.defaultCellWidthWithExtraSize(
            configuration = configuration,
            totalColumns = totalColumns,
            hasExtra = hasTotal
        )
        val rowHeaderWidth = tableDimensions.defaultRowHeaderCellWidthWithExtraSize(
            configuration = configuration,
            totalColumns = totalColumns,
            hasExtra = hasTotal
        )
        val headerCellWidthList = headerRowColumns.map { headerParentRowColumns ->
            tableDimensions.headerCellWidth(
                headerRowColumns = headerParentRowColumns,
                configuration = configuration,
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
