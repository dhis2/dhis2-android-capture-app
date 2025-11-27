package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData

internal class GetDataSetSectionData(
    private val datasetUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(sectionUid: String): DataSetInstanceSectionData {
        val dataSetInstanceConfiguration =
            dataSetInstanceRepository.dataSetInstanceConfiguration(
                datasetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
                sectionUid,
            )

        val dataSetInstanceSectionConfiguration =
            dataSetInstanceRepository.dataSetInstanceSectionConfiguration(
                sectionUid,
            )

        val data =
            dataSetInstanceRepository.getDataSetInstanceSectionCells(
                dataSetInstanceConfiguration.allDataSetElements,
                datasetUid,
                sectionUid,
            )

        return DataSetInstanceSectionData(
            dataSetInstanceConfiguration = dataSetInstanceConfiguration,
            dataSetInstanceSectionConfiguration = dataSetInstanceSectionConfiguration,
            tableGroups = data,
        )
    }
}
