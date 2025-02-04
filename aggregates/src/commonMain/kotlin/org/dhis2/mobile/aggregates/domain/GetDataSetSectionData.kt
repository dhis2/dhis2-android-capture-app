package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository

internal class GetDataSetSectionData(
    private val datasetUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(sectionUid: String) =
        dataSetInstanceRepository.getDataSetSectionData(
            dataSetUid = datasetUid,
            orgUnitUid = orgUnitUid,
            periodId = periodId,
            attrOptionComboUid = attrOptionComboUid,
            sectionUid = sectionUid,
        )
}
