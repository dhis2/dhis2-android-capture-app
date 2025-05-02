package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.ui.inputs.TableId

internal class SetDataValue(
    val dataSetUid: String,
    val periodId: String,
    val orgUnitUid: String,
    val attrOptionComboUid: String,
    val repository: DataSetInstanceRepository,
) : ValueValidator(repository) {
    suspend operator fun invoke(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
        value: String?,
    ): Result<Unit> {
        val dataElementUid = checkOnlyOneDataElementIsProvided(rowIds, columnIds)
        val categoryOptionComboUid = checkedCategoryOptionCombos(dataSetUid, dataElementUid, rowIds, columnIds)

        return repository.updateValue(
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUid = dataElementUid,
            categoryOptionComboUid = categoryOptionComboUid,
            value = value,
        )
    }
}
