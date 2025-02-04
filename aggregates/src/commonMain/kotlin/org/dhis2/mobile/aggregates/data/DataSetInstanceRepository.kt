package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

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

    fun getRenderingConfig(
        dataSetUid: String,
    ): DataSetRenderingConfig

    suspend fun getDataSetSectionData(
        dataSetUid: String,
        orgUnitUid: String,
        periodId: String,
        attrOptionComboUid: String,
        sectionUid: String,
    ): List<TableModel>
}
