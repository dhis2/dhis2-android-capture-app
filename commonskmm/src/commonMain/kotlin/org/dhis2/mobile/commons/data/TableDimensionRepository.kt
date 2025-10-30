package org.dhis2.mobile.commons.data

interface TableDimensionRepository {
    suspend fun saveWidthForSection(
        tableId: String,
        sectionUid: String,
        widthDpValue: Float,
    )

    suspend fun saveColumnWidthForSection(
        tableId: String,
        sectionUid: String,
        column: Int,
        widthDpValue: Float,
    )

    suspend fun saveTableWidth(
        tableId: String,
        sectionUid: String,
        widthDpValue: Float,
    )

    suspend fun resetTable(
        tableId: String,
        sectionUid: String,
    )

    suspend fun getTableWidth(sectionUid: String): Map<String, Float>?

    suspend fun getWidthForSection(sectionUid: String): Map<String, Float>?

    suspend fun getColumnWidthForSection(
        tableList: List<String>,
        sectionUid: String,
    ): Map<String, Map<Int, Float>>?

    fun columnWidthDataStoreKeysForDataSet(
        dataSetUid: String,
        sectionUid: String,
    ) = "col_width_${dataSetUid}_${sectionUid}_%"

    fun columnWidthDataStoreKeysForTable(
        dataSetUid: String,
        tableId: String,
        sectionUid: String,
    ) = "col_width_${dataSetUid}_${sectionUid}_${tableId}_%"

    fun rowHeaderWidthDataStoreKeyForDataSet(
        dataSetUid: String,
        sectionUid: String,
    ) = "row_width_${dataSetUid}_${sectionUid}_%"

    fun columnWidthDataStoreKey(
        dataSetUid: String,
        sectionUid: String,
        tableId: String,
        column: Int,
    ) = "col_width_${dataSetUid}_${sectionUid}_${tableId}_$column"

    fun rowHeaderWidthDataStoreKey(
        dataSetUid: String,
        tableId: String,
        sectionUid: String,
    ) = "row_width_${dataSetUid}_${sectionUid}_$tableId"

    fun tableExtraWidthDataStoreKey(
        dataSetUid: String,
        sectionUid: String,
        tableId: String,
    ) = "table_width_${dataSetUid}_${sectionUid}_$tableId"

    fun tableExtraWidthForDataSet(
        dataSetUid: String,
        sectionUid: String,
    ) = "table_width_${dataSetUid}_${sectionUid}_%"
}
