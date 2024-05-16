package org.dhis2.composetable.model

import kotlinx.serialization.Serializable

@Serializable
data class TableHeader(val rows: List<TableHeaderRow>, val hasTotals: Boolean = false) {
    fun numberOfColumns(rowIndex: Int): Int {
        var totalCells = 1
        for (index in 0 until rowIndex + 1) {
            totalCells *= rows[index].cells.size
        }
        return totalCells
    }

    fun tableMaxColumns() = numberOfColumns(rows.size - 1)
}
