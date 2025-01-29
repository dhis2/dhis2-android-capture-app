package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection

internal interface DataSetInstanceRepository {
    fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): DataSetDetails

    fun getDataSetInstanceSections(
        dataSetUid: String,
    ): List<DataSetSection>

    abstract fun getRenderingConfig(
        dataSetUid: String,
    ): DataSetRenderingConfig
}
