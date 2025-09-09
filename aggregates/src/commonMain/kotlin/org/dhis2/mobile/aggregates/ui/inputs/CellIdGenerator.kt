package org.dhis2.mobile.aggregates.ui.inputs

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
internal object CellIdGenerator {
    private val idExtractorValidator = Regex("<(co|de|coc)>(.{11})")

    fun generateId(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
        val id =
            rowIds.joinToString { it.join() } +
                ":" +
                columnIds.joinToString { it.join() }

        return Base64.encode(id.encodeToByteArray())
    }

    fun getIdInfo(id: String): Pair<List<TableId>, List<TableId>> {
        val decoded = Base64.decode(id).decodeToString()
        val (rowId, columnId) = decoded.split(":")

        val rowIds = rowId.toTableIdList()
        val columnIds = columnId.toTableIdList()
        return Pair(rowIds, columnIds)
    }

    fun totalHeaderRowId(tableId: String) = "${tableId}_totals"

    fun totalRow(
        tableId: String,
        rowIndex: Int,
    ) = "${tableId}_${rowIndex}_totals"

    fun totalCellId(
        tableId: String,
        columnIndex: Int,
    ) = "${tableId}_totals_$columnIndex"

    fun totalId(tableId: String) = "${tableId}_total_total"

    private fun String.toTableIdList() =
        idExtractorValidator
            .findAll(this)
            .map { match ->
                val type = match.groupValues[1]
                val id = match.groupValues[2]
                TableId(
                    id = id,
                    type = TableIdType.fromIdentifier(type),
                )
            }.toList()
}

internal data class TableId(
    val id: String,
    val type: TableIdType,
) {
    fun join() = "<${type.identifier}>$id"
}

sealed class TableIdType(
    val identifier: String,
) {
    data object DataElement : TableIdType("de")

    data object CategoryOption : TableIdType("co")

    data object CategoryOptionCombo : TableIdType("coc")

    companion object {
        fun fromIdentifier(identifier: String) =
            when (identifier) {
                DataElement.identifier -> DataElement
                CategoryOption.identifier -> CategoryOption
                CategoryOptionCombo.identifier -> CategoryOptionCombo
                else -> throw IllegalArgumentException("Invalid identifier: $identifier")
            }
    }
}
