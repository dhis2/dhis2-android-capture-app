package org.dhis2.utils.granularsync

import org.hisp.dhis.android.core.common.State

data class SyncStatusItem(
    val type: SyncStatusType,
    val displayName: String,
    val description: String,
    val state: State
)

sealed class SyncStatusType {
    data class TrackerProgram(val programUid: String) : SyncStatusType()
    data class TrackedEntity(val teiUid: String, val enrollmentUid: String) : SyncStatusType()
    data class EventProgram(val programUid: String) : SyncStatusType()
    data class Event(val eventUid: String, val programStageUid: String) : SyncStatusType()
    data class DataSet(val dataSetUid: String) : SyncStatusType()
    data class DataSetInstance(val dataSetInstance: String) : SyncStatusType()
}
