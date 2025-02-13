package org.dhis2.mobile.aggregates.model

internal data class DataSetInstanceConfiguration(
    val hasDataElementDecoration: Boolean,
    val compulsoryDataElements: List<MandatoryCellElements>,
    val allDataSetElements: List<CellElement>,
    val greyedOutFields: List<String>,
    val editable: Boolean,
) {
    fun isCellEditable(rowId: String) =
        editable && !greyedOutFields.contains(rowId)

    fun isMandatory(rowId: String, columnId: String) =
        compulsoryDataElements.find {
            it.categoryOptionComboUid == columnId &&
                it.uid == rowId
        } != null
}

internal data class CellElement(
    val uid: String,
    val categoryComboUid: String?,
    val label: String,
    val description: String?,
    val isMultiText: Boolean,
)

internal data class MandatoryCellElements(
    val uid: String?,
    val categoryOptionComboUid: String?,
)
