package org.dhis2.mobile.aggregates.ui.inputs

sealed class ResizeAction {
    data class RowHeaderChanged(
        val tableId: String,
        val sectionId: String,
        val newValue: Float,
    ) : ResizeAction()

    data class ColumnHeaderChanged(
        val tableId: String,
        val sectionId: String,
        val columns: Int,
        val newValue: Float,
    ) : ResizeAction()

    data class TableDimension(
        val tableId: String,
        val sectionId: String,
        val newValue: Float,
    ) : ResizeAction()

    data class Reset(
        val tableId: String,
        val sectionId: String,
    ) : ResizeAction()

    data class GetTableSavedWidth(
        val sectionId: String,
    ) : ResizeAction()

    data class GetRowHeaderSavedWidth(
        val sectionId: String,
    ) : ResizeAction()

    data class GetColumSavedWidth(
        val sectionId: String,
    ) : ResizeAction()
}
