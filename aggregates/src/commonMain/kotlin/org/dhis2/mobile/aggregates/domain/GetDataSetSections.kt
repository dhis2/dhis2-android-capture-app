package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetSection

internal class GetDataSetSections(
    private val dataSetUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(): List<DataSetSection> =
        dataSetInstanceRepository.getDataSetInstanceSections(
            dataSetUid = dataSetUid,
        )
}
