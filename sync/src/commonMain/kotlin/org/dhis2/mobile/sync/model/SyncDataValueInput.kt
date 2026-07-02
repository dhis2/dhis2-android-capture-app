package org.dhis2.mobile.sync.model

import org.dhis2.mobile.sync.domain.DataSetUid

internal typealias OrgUnitUid = String
internal typealias CategoryOptionComboUid = String
internal typealias PeriodId = String

internal data class SyncDataValueInput(
    val dataSetUid: DataSetUid,
    val orgUnitUid: OrgUnitUid,
    val periodId: PeriodId,
    val attrOptionComboUid: CategoryOptionComboUid,
    val categoryOptionComboUid: List<CategoryOptionComboUid>,
)
