package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.Conflicts
import org.dhis2.mobile.aggregates.model.DataValueData
import org.dhis2.mobile.commons.extensions.userFriendlyValue

internal class GetDataValueData(
    private val datasetUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {

    suspend operator fun invoke(
        dataElementUids: List<String>,
    ): Map<Pair<String, String>, DataValueData> {
        return dataSetInstanceRepository.values(
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUids = dataElementUids,
        ).associate { (key, value) ->
            key to DataValueData(
                value = value?.userFriendlyValue(key.first),
                conflicts = conflicts(key.first, key.second),
            )
        }
    }

    private suspend fun conflicts(
        dataElementUid: String,
        categoryOptionComboUid: String,
    ) = dataSetInstanceRepository.conflicts(
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
