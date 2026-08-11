package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.domain.DataSetUid
import org.dhis2.mobile.sync.model.CategoryOptionComboUid
import org.dhis2.mobile.sync.model.OrgUnitUid
import org.dhis2.mobile.sync.model.PeriodId

internal interface SyncDataValueRepository {
    suspend fun uploadDataValues(
        dataSetUid: DataSetUid,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attributeOptionComboUid: CategoryOptionComboUid,
        categoryOptionComboUids: List<CategoryOptionComboUid>,
    )

    suspend fun uploadCompleteRegistrations(
        dataSetUid: DataSetUid,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attributeOptionComboUid: CategoryOptionComboUid,
    )

    suspend fun downloadDataValues(
        dataSetUid: DataSetUid,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attributeOptionComboUid: CategoryOptionComboUid,
    )
}
