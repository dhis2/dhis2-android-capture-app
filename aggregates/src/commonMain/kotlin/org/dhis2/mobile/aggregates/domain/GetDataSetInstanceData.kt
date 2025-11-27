package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.DataSetInstanceData

internal class GetDataSetInstanceData(
    private val datasetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val dataSetInstanceRepository: DataSetInstanceRepository,
    private val openErrorLocation: Boolean,
) {
    suspend operator fun invoke(scope: CoroutineScope): DataSetInstanceData =
        with(scope) {
            val dataSetDetails =
                async {
                    dataSetInstanceRepository.getDataSetInstance(
                        dataSetUid = datasetUid,
                        periodId = periodId,
                        orgUnitUid = orgUnitUid,
                        attrOptionComboUid = attrOptionComboUid,
                    )
                }
            val dataSetRenderingConfig =
                async {
                    dataSetInstanceRepository.getRenderingConfig(datasetUid)
                }
            val dataSetSections =
                async {
                    dataSetInstanceRepository.getDataSetInstanceSections(datasetUid)
                }

            val initialSectionToLoad =
                async {
                    dataSetInstanceRepository.getInitialSectionToLoad(
                        openErrorLocation = openErrorLocation,
                        dataSetUid = datasetUid,
                        periodId = periodId,
                        orgUnitUid = orgUnitUid,
                        catOptCombo = attrOptionComboUid,
                    )
                }
            DataSetInstanceData(
                dataSetDetails = dataSetDetails.await(),
                dataSetRenderingConfig = dataSetRenderingConfig.await(),
                dataSetSections = dataSetSections.await(),
                initialSectionToLoad = initialSectionToLoad.await(),
            )
        }
}
