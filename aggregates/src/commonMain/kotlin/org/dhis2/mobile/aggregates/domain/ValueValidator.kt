package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType

internal abstract class ValueValidator(
    private val repository: DataSetInstanceRepository,
) {
    suspend fun checkedCategoryOptionCombos(
        dataSetUid: String,
        dataElementUid: String,
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
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
            categoryOptionCombos.isNotEmpty() -> categoryOptionCombos.first()
            else -> repository.categoryOptionComboFromCategoryOptions(dataSetUid, dataElementUid, categoryOptions)
        }
    }

    fun checkOnlyOneDataElementIsProvided(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
        val dataElementUids =
            rowIds.filter { it.type is TableIdType.DataElement }.map { it.id } +
                columnIds.filter { it.type is TableIdType.DataElement }.map { it.id }

        if (dataElementUids.size != 1) throw IllegalStateException("Only one data element can be provided")

        return dataElementUids.first()
    }
}
