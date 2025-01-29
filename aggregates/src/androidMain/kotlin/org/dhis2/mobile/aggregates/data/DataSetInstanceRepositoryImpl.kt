package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.data.mappers.toDataSetDetails
import org.dhis2.mobile.aggregates.data.mappers.toDataSetSection
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.dataset.Section

internal class DataSetInstanceRepositoryImpl(
    private val d2: D2,
) : DataSetInstanceRepository {

    override fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ) = d2.dataSetModule().dataSetInstances()
        .byDataSetUid().eq(dataSetUid)
        .byPeriod().eq(periodId)
        .byOrganisationUnitUid().eq(orgUnitUid)
        .byAttributeOptionComboUid().eq(attrOptionComboUid)
        .blockingGet()
        .map(DataSetInstance::toDataSetDetails)
        .first()

    override fun getDataSetInstanceSections(
        dataSetUid: String,
    ) = d2.dataSetModule().sections()
        .byDataSetUid().eq(dataSetUid)
        .blockingGet().map(Section::toDataSetSection)

    override fun getRenderingConfig(
        dataSetUid: String,
    ) = d2.dataSetModule().dataSets()
        .uid(dataSetUid)
        .blockingGet()?.let {
            DataSetRenderingConfig(
                useVerticalTabs = it.renderHorizontally() != true,
            )
        } ?: DataSetRenderingConfig(
        useVerticalTabs = true,
    )
}
