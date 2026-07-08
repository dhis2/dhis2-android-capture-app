package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.domain.DataSetUid
import org.dhis2.mobile.sync.model.CategoryOptionComboUid
import org.dhis2.mobile.sync.model.OrgUnitUid
import org.dhis2.mobile.sync.model.PeriodId
import org.hisp.dhis.android.core.D2

internal class SyncDataValueRepositoryImpl(
    private val d2: D2,
) : SyncDataValueRepository {
    override suspend fun uploadDataValues(
        dataSetUid: DataSetUid,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attributeOptionComboUid: CategoryOptionComboUid,
        categoryOptionComboUids: List<CategoryOptionComboUid>,
    ) {
        d2
            .dataValueModule()
            .dataValues()
            .byAttributeOptionComboUid()
            .eq(attributeOptionComboUid)
            .byOrganisationUnitUid()
            .eq(orgUnitUid)
            .byPeriod()
            .eq(periodId)
            .byCategoryOptionComboUid()
            .`in`(categoryOptionComboUids)
            .blockingUpload()
    }

    override suspend fun uploadCompleteRegistrations(
        dataSetUid: DataSetUid,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attributeOptionComboUid: CategoryOptionComboUid,
    ) {
        d2
            .dataSetModule()
            .dataSetCompleteRegistrations()
            .byDataSetUid()
            .eq(dataSetUid)
            .byAttributeOptionComboUid()
            .eq(attributeOptionComboUid)
            .byOrganisationUnitUid()
            .eq(orgUnitUid)
            .byPeriod()
            .eq(periodId)
            .blockingUpload()
    }

    // TODO: The sdk does not provide methods to download data values for a given data set instance
    override suspend fun downloadDataValues(
        dataSetUid: DataSetUid,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attributeOptionComboUid: CategoryOptionComboUid,
    ) {
        // Do nothing
    }
}
