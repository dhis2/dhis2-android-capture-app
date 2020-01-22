package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel

data class DataSetTable(
    val dataTableModel: DataTableModel,
    val fields: List<List<FieldViewModel>>,
    val cells: List<List<String>>,
    val accessDataWrite: Boolean
)