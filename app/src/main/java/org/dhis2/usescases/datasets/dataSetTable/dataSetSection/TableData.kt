package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.bindings.maxLengthLabel
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel

data class TableData(
    val dataTableModel: DataTableModel,
    val fieldViewModels: List<List<FieldViewModel>>,
    val cells: List<List<String>>,
    val accessDataWrite: Boolean,
    val showRowTotals: Boolean = false,
    val showColumnTotals: Boolean = false,
    val hasDataElementDecoration: Boolean,
) {
    fun columnHeaders() = dataTableModel.header
    fun catCombo() = dataTableModel.catCombo
    fun maxLengthLabel() = dataTableModel.rows?.maxLengthLabel()
    fun maxColumns() = dataTableModel.header!![dataTableModel.header.size - 1].size
    fun rows() = dataTableModel.rows
}
