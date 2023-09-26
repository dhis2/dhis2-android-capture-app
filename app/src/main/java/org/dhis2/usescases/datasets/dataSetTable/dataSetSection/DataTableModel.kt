package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementOperand

data class DataTableModel(
    val periodId: String,
    val orgUnitUid: String,
    val attributeOptionComboUid: String,
    val rows: MutableList<DataElement>?,
    val dataValues: MutableList<DataSetTableModel>?,
    val dataElementDisabled: List<DataElementOperand>?,
    val compulsoryCells: List<DataElementOperand>?,
    val catCombo: CategoryCombo?,
    val header: MutableList<MutableList<CategoryOption>>?,
    val catOptionOrder: List<List<CategoryOption>>?,
)
