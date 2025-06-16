package org.dhis2.mobile.aggregates.model

internal data class DataSetInstanceConfiguration(
    val hasDataElementDecoration: Boolean,
    val compulsoryDataElements: List<MandatoryCellElements>,
    val allDataSetElements: List<CellElement>,
    val greyedOutFields: List<GreyedOutField>,
    val editable: Boolean,
) {
    fun isCellEditable(
        dataElementUid: String,
        categoryOptionComboUid: String?,
        categoryOptionComboUids: List<String>?,
    ): Boolean {
        val isGreyedOut = greyedOutFields.find {
            if (categoryOptionComboUid != null) {
                it.dataElementUid == dataElementUid && it.categoryOptionComboUid == categoryOptionComboUid
            } else {
                it.dataElementUid == dataElementUid && it.categoryOptionUids == categoryOptionComboUids
            }
        } != null

        return editable && isGreyedOut.not()
    }

    fun isMandatory(rowId: String, columnId: String) =
        compulsoryDataElements.find {
            it.categoryOptionComboUid == columnId &&
                it.uid == "$rowId.$columnId"
        } != null
}

internal data class CellElement(
    val uid: String,
    val categoryComboUid: String?,
    val label: String,
    val description: String?,
    val isMultiText: Boolean,
    val disabled: Boolean = false,
)

internal data class MandatoryCellElements(
    val uid: String?,
    val categoryOptionComboUid: String?,
)

internal data class GreyedOutField(
    val dataElementUid: String,
    val categoryOptionComboUid: String,
    val categoryOptionUids: List<String>,
)
