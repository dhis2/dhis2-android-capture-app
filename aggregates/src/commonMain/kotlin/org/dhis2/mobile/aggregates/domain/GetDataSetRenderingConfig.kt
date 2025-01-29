package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository

class GetDataSetRenderingConfig(
    private val datasetUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke() = dataSetInstanceRepository
        .getRenderingConfig(datasetUid)
}
