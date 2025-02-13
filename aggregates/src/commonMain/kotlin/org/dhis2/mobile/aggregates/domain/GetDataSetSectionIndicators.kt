package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository

internal class GetDataSetSectionIndicators(
    val dataSetUid: String,
    val periodId: String,
    val orgUnitUid: String,
    val attributeOptionComboUid: String,
    val repository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(sectionUid: String) = repository.getDataSetIndicator(
        dataSetUid = dataSetUid,
        periodId = periodId,
        orgUnitUid = orgUnitUid,
        attributeOptionComboUid = attributeOptionComboUid,
        sectionUid = sectionUid,
    )
}
