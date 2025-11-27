package org.dhis2.mobile.commons.data

import org.hisp.dhis.android.core.D2

class TableDimensionRepositoryImpl(
    private val d2: D2,
    private val dataSetUid: String,
) : TableDimensionRepository {
    override suspend fun saveWidthForSection(
        tableId: String,
        sectionUid: String,
        widthDpValue: Float,
    ) {
        d2
            .dataStoreModule()
            .localDataStore()
            .value(
                rowHeaderWidthDataStoreKey(
                    dataSetUid,
                    tableId,
                    sectionUid,
                ),
            ).blockingSet(widthDpValue.toString())
    }

    override suspend fun saveColumnWidthForSection(
        tableId: String,
        sectionUid: String,
        column: Int,
        widthDpValue: Float,
    ) {
        d2
            .dataStoreModule()
            .localDataStore()
            .value(columnWidthDataStoreKey(dataSetUid, sectionUid, tableId, column))
            .blockingSet(widthDpValue.toString())
    }

    override suspend fun saveTableWidth(
        tableId: String,
        sectionUid: String,
        widthDpValue: Float,
    ) {
        d2
            .dataStoreModule()
            .localDataStore()
            .value(tableExtraWidthDataStoreKey(dataSetUid, sectionUid, tableId))
            .blockingSet(widthDpValue.toString())
    }

    override suspend fun resetTable(
        tableId: String,
        sectionUid: String,
    ) {
        d2
            .dataStoreModule()
            .localDataStore()
            .byKey()
            .like(rowHeaderWidthDataStoreKey(dataSetUid, tableId, sectionUid))
            .blockingGet()
            .forEach { keyValuePair ->
                keyValuePair.key()?.let { key ->
                    clearKey(key)
                }
            }

        d2
            .dataStoreModule()
            .localDataStore()
            .byKey()
            .like(columnWidthDataStoreKeysForTable(dataSetUid, tableId, sectionUid))
            .blockingGet()
            .forEach { keyValuePair ->
                keyValuePair.key()?.let { key ->
                    clearKey(key)
                }
            }
        d2
            .dataStoreModule()
            .localDataStore()
            .byKey()
            .like(tableExtraWidthDataStoreKey(dataSetUid, sectionUid, tableId))
            .blockingGet()
            .forEach { keyValuePair ->
                keyValuePair.key()?.let { key ->
                    clearKey(key)
                }
            }
    }

    override suspend fun getTableWidth(sectionUid: String): Map<String, Float>? =
        d2
            .dataStoreModule()
            .localDataStore()
            .byKey()
            .like(tableExtraWidthForDataSet(dataSetUid, sectionUid))
            .blockingGet()
            .mapNotNull {
                val key = it.key()
                val metadata = key?.split("_")
                val tableId = metadata?.get(metadata.size - 1)
                val value =
                    tableId?.let {
                        d2
                            .dataStoreModule()
                            .localDataStore()
                            .value(tableExtraWidthDataStoreKey(tableId, sectionUid, tableId))
                            .blockingGet()
                            ?.value()
                            ?.toFloatOrNull()
                    }
                if (tableId != null && value != null) {
                    tableId to value
                } else {
                    null
                }
            }.toMap()
            .takeIf { it.isNotEmpty() }

    override suspend fun getWidthForSection(sectionUid: String): Map<String, Float>? =
        d2
            .dataStoreModule()
            .localDataStore()
            .byKey()
            .like(rowHeaderWidthDataStoreKeyForDataSet(dataSetUid, sectionUid))
            .blockingGet()
            .mapNotNull {
                val key = it.key()
                val metadata = key?.split("_")
                val tableId = metadata?.get(metadata.size - 1)
                val value =
                    tableId?.let {
                        d2
                            .dataStoreModule()
                            .localDataStore()
                            .value(rowHeaderWidthDataStoreKey(dataSetUid, tableId, sectionUid))
                            .blockingGet()
                            ?.value()
                            ?.toFloatOrNull()
                    }
                if (tableId != null && value != null) {
                    tableId to value
                } else {
                    null
                }
            }.toMap()
            .takeIf { it.isNotEmpty() }

    override suspend fun getColumnWidthForSection(
        tableList: List<String>,
        sectionUid: String,
    ): Map<String, Map<Int, Float>>? =
        if (tableList.isNotEmpty()) {
            columnWidthForTableModels(tableList, sectionUid)
        } else {
            columnWidthForDataSet(sectionUid)
        }

    private fun columnWidthForTableModels(
        tableList: List<String>,
        sectionUid: String,
    ): Map<String, Map<Int, Float>>? {
        val map =
            tableList.associateWith { tableId ->
                val valueMap =
                    d2
                        .dataStoreModule()
                        .localDataStore()
                        .byKey()
                        .like(columnWidthDataStoreKeysForTable(dataSetUid, tableId, sectionUid))
                        .blockingGet()
                        .mapNotNull {
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

    private fun columnWidthForDataSet(sectionUid: String): Map<String, Map<Int, Float>>? {
        val tableLists =
            d2
                .dataStoreModule()
                .localDataStore()
                .byKey()
                .like(columnWidthDataStoreKeysForDataSet(dataSetUid, sectionUid))
                .blockingGet()
                .mapNotNull {
                    val key = it.key()
                    val metadata = key?.split("_")
                    val tableId = metadata?.get(metadata.size - 2)
                    tableId
                }
        return columnWidthForTableModels(tableLists, sectionUid)
    }

    private fun clearKey(key: String) {
        d2
            .dataStoreModule()
            .localDataStore()
            .value(key)
            .blockingDeleteIfExist()
    }
}
