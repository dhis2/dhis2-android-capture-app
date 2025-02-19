package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType

internal class SetDataValue(
    val periodId: String,
    val orgUnitUid: String,
    val attrOptionComboUid: String,
    val repository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
        value: String?,
    ): Result<Unit> {
        val dataElementUid = checkOnlyOneDataElementIsProvided(rowIds, columnIds)
        val categoryOptionComboUid = checkedCategoryOptionCombos(rowIds, columnIds)

        // TODO:

        return repository.updateValue(
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUid = dataElementUid,
            categoryOptionComboUid = categoryOptionComboUid,
            value = value,
        )
    }

    private fun checkOnlyOneDataElementIsProvided(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
        val dataElementUids = rowIds.filter { it.type is TableIdType.DataElement }.map { it.id } +
            columnIds.filter { it.type is TableIdType.DataElement }.map { it.id }

        if (dataElementUids.size != 1) throw IllegalStateException("Only one data element can be provided")

        return dataElementUids.first()
    }

    private suspend fun checkedCategoryOptionCombos(
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
            else -> repository.categoryOptionComboFromCategoryOptions(categoryOptions)
        }
    }
}
