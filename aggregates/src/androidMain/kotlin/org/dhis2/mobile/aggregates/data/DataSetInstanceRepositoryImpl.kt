package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.data.mappers.toDataSetDetails
import org.hisp.dhis.android.core.D2

class DataSetInstanceRepositoryImpl(
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
        .blockingGet().map {
            it.toDataSetDetails()
        }.first()
}
