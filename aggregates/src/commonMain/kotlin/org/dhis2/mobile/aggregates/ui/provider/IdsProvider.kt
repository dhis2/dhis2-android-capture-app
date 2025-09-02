package org.dhis2.mobile.aggregates.ui.provider

import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType

internal object IdsProvider {
    fun getDataElementUid(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
        val dataElementUids =
            rowIds.filter { it.type is TableIdType.DataElement }.map { it.id } +
                columnIds.filter { it.type is TableIdType.DataElement }.map { it.id }

        if (dataElementUids.size != 1) throw IllegalStateException("Only one data element can be provided")

        return dataElementUids.first()
    }

    fun getCategoryOptionCombo(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): Pair<String?, List<String>> {
        val categoryOptions =
            rowIds.filter { it.type is TableIdType.CategoryOption }.map { it.id } +
                columnIds.filter { it.type is TableIdType.CategoryOption }.map { it.id }
        val categoryOptionCombos =
            rowIds.filter { it.type is TableIdType.CategoryOptionCombo }.map { it.id } +
                columnIds.filter { it.type is TableIdType.CategoryOptionCombo }.map { it.id }

        if (categoryOptions.isNotEmpty() && categoryOptionCombos.isNotEmpty()) {
            throw IllegalStateException(
                "Category options and category option combos cannot be provided at the same time",
            )
        }
        if (categoryOptionCombos.size > 1) throw IllegalStateException("Only one category option combo can be provided")

        return when {
            categoryOptionCombos.isNotEmpty() -> Pair(categoryOptionCombos.first(), emptyList())
            else -> Pair(null, categoryOptions)
        }
    }
}
