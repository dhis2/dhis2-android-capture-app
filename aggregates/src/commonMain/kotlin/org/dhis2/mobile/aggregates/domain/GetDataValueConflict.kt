package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.Conflicts

internal class GetDataValueConflict(
    private val datasetUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(
        dataElementUid: String,
        categoryOptionComboUid: String,
    ) =
        dataSetInstanceRepository.conflicts(
            dataSetUid = datasetUid,
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUid = dataElementUid,
            categoryOptionComboUid = categoryOptionComboUid,
        ).let { (errors, warnings) ->
            Conflicts(
                errors = errors,
                warnings = warnings,
            )
        }
}
