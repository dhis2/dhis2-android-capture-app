package org.dhis2.utils.granularsync

import org.hisp.dhis.android.core.common.State

data class SyncStatusItem(
    val type: SyncStatusType,
    val displayName: String,
    val description: String,
    val state: State
)

sealed class SyncStatusType {
    data class TrackerProgram(val programUid: String, val trackedEntityTypeUid: String) : SyncStatusType()
    data class TrackedEntity(
        val teiUid: String,
        val programUid: String?,
        val enrollmentUid: String?
    ) : SyncStatusType()

    data class Enrollment(
        val enrollmentUid: String,
        val programUid: String
    ) : SyncStatusType()

    data class EventProgram(val programUid: String) : SyncStatusType()
    data class Event(
        val eventUid: String,
        val programUid: String,
        val programStageUid: String?,
        val hasNullDataElementConflict: Boolean = false
    ) : SyncStatusType()

    data class DataSet(val dataSetUid: String) : SyncStatusType()
    data class DataSetInstance(
        val dataSetUid: String,
        val orgUnitUid: String,
        val periodId: String,
        val attrOptComboUid: String
    ) : SyncStatusType()
}
