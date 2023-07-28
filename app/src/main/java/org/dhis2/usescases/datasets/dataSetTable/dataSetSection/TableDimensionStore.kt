package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.composetable.model.TableModel
import org.hisp.dhis.android.core.D2

class TableDimensionStore(
    val d2: D2,
    val dataSetUid: String,
    val sectionUid: String
) {

    fun saveWidthForSection(tableId: String, widthDpValue: Float) {
        d2.dataStoreModule().localDataStore()
            .value(rowHeaderWidthDataStoreKey(tableId))
            .blockingSet(widthDpValue.toString())
    }

    fun saveColumnWidthForSection(tableId: String, column: Int, widthDpValue: Float) {
        d2.dataStoreModule().localDataStore()
            .value(columnWidthDataStoreKey(tableId, column))
            .blockingSet(widthDpValue.toString())
    }

    fun saveTableWidth(tableId: String, widthDpValue: Float) {
        d2.dataStoreModule().localDataStore()
            .value(tableExtraWidthDataStoreKey(tableId))
            .blockingSet(widthDpValue.toString())
    }

    fun resetTable(tableId: String) {
        d2.dataStoreModule().localDataStore()
            .byKey().like(rowHeaderWidthDataStoreKey(tableId))
            .blockingGet().forEach { keyValuePair ->
                keyValuePair.key()?.let { key ->
                    clearKey(key)
                }
            }

        d2.dataStoreModule().localDataStore()
            .byKey().like(columnWidthDataStoreKeysForTable(tableId))
            .blockingGet().forEach { keyValuePair ->
                keyValuePair.key()?.let { key ->
                    clearKey(key)
                }
            }
        d2.dataStoreModule().localDataStore()
            .byKey().like(tableExtraWidthDataStoreKey(tableId))
            .blockingGet().forEach { keyValuePair ->
                keyValuePair.key()?.let { key ->
                    clearKey(key)
                }
            }
    }

    private fun clearKey(key: String) {
        d2.dataStoreModule().localDataStore()
            .value(key).blockingDeleteIfExist()
    }

    fun getTableWidth(): Map<String, Float>? {
        return d2.dataStoreModule().localDataStore()
            .byKey().like(tableExtraWidthForDataSet())
            .blockingGet().mapNotNull {
                val key = it.key()
                val metadata = key?.split("_")
                val tableId = metadata?.get(metadata.size - 1)
                val value = tableId?.let {
                    d2.dataStoreModule().localDataStore()
                        .value(tableExtraWidthDataStoreKey(tableId))
                        .blockingGet()?.value()?.toFloatOrNull()
                }
                if (tableId != null && value != null) {
                    tableId to value
                } else {
                    null
                }
            }.toMap().takeIf { it.isNotEmpty() }
    }

    fun getWidthForSection(): Map<String, Float>? {
        return d2.dataStoreModule().localDataStore()
            .byKey().like(rowHeaderWidthDataStoreKeyForDataSet())
            .blockingGet().mapNotNull {
                val key = it.key()
                val metadata = key?.split("_")
                val tableId = metadata?.get(metadata.size - 1)
                val value = tableId?.let {
                    d2.dataStoreModule().localDataStore()
                        .value(rowHeaderWidthDataStoreKey(tableId))
                        .blockingGet()?.value()?.toFloatOrNull()
                }
                if (tableId != null && value != null) {
                    tableId to value
                } else {
                    null
                }
            }.toMap().takeIf { it.isNotEmpty() }
    }

    fun getColumnWidthForSection(
        tableList: MutableList<TableModel>?
    ): Map<String, Map<Int, Float>>? {
        return if (tableList != null) {
            columnWidthForTableModels(tableList.map { it.id ?: "" })
        } else {
            columnWidthForDataSet()
        }
    }

    private fun columnWidthForTableModels(tableList: List<String>): Map<String, Map<Int, Float>>? {
        val map = tableList.associateWith { tableId ->
            val valueMap = d2.dataStoreModule().localDataStore()
                .byKey().like(columnWidthDataStoreKeysForTable(tableId))
                .blockingGet().mapNotNull {
                    val key = it.key()
                    val column = key?.split("_")?.lastOrNull()?.toInt()
                    val value = it.value()?.toFloatOrNull()
                    if (column != null && value != null) {
                        column to value
                    } else {
                        null
                    }
                }.toMap()
            valueMap
        }

        return map.takeIf { it.isNotEmpty() }
    }

    private fun columnWidthForDataSet(): Map<String, Map<Int, Float>>? {
        val tableLists = d2.dataStoreModule().localDataStore()
            .byKey().like(columnWidthDataStoreKeysForDataSet())
            .blockingGet().mapNotNull {
                val key = it.key()
                val metadata = key?.split("_")
                val tableId = metadata?.get(metadata.size - 2)
                tableId
            }
        return columnWidthForTableModels(tableLists)
    }

    private fun columnWidthDataStoreKeysForDataSet() = "col_width_${dataSetUid}_${sectionUid}_%"

    private fun columnWidthDataStoreKeysForTable(tableId: String) =
        "col_width_${dataSetUid}_${sectionUid}_${tableId}_%"

    private fun rowHeaderWidthDataStoreKeyForDataSet() = "row_width_${dataSetUid}_${sectionUid}_%"

    private fun columnWidthDataStoreKey(tableId: String, column: Int) =
        "col_width_${dataSetUid}_${sectionUid}_${tableId}_$column"

    private fun rowHeaderWidthDataStoreKey(tableId: String) =
        "row_width_${dataSetUid}_${sectionUid}_$tableId"
    private fun tableExtraWidthDataStoreKey(tableId: String) =
        "table_width_${dataSetUid}_${sectionUid}_$tableId"
    private fun tableExtraWidthForDataSet() = "table_width_${dataSetUid}_${sectionUid}_%"
}
