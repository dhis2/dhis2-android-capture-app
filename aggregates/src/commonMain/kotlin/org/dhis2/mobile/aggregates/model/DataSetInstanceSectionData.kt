package org.dhis2.mobile.aggregates.model

internal data class DataSetInstanceSectionData(
    private val dataSetInstanceConfiguration: DataSetInstanceConfiguration,
    private val dataSetInstanceSectionConfiguration: DataSetInstanceSectionConfiguration?,
    val tableGroups: List<TableGroup>,
) {
    fun hasDecoration() = dataSetInstanceConfiguration.hasDataElementDecoration

    fun isEditable(
        dataElementUid: String,
        categoryOptionComboUid: String?,
        categoryOptionComboUids: List<String>?,
    ) = dataSetInstanceConfiguration.isCellEditable(
        dataElementUid,
        categoryOptionComboUid,
        categoryOptionComboUids,
    )

    fun isMandatory(
        rowId: String,
        columnId: String,
    ) = dataSetInstanceConfiguration.isMandatory(
        rowId = rowId,
        columnId = columnId,
    )

    fun showRowTotals() = dataSetInstanceSectionConfiguration?.showRowTotals ?: false

    fun showColumnTotals() = dataSetInstanceSectionConfiguration?.showColumnTotals ?: false

    fun pivotedHeaderId() = dataSetInstanceSectionConfiguration?.pivotedHeaderId
}

internal data class TableGroup(
    val uid: String,
    val label: String,
    val subgroups: List<String>,
    val cellElements: List<CellElement>,
    val headerRows: List<List<CellElement>>,
    val headerCombinations: List<String>,
    val pivotMode: PivoteMode,
)

internal sealed interface PivoteMode {
    data object None : PivoteMode

    data class CategoryToColumn(
        val pivotedHeaders: List<CellElement>,
    ) : PivoteMode

    data object Transpose : PivoteMode
}
