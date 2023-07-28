package org.dhis2.commons.sync

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class SyncContext : Parcelable {

    @Parcelize
    data class Global(val uid: String = "") : SyncContext()

    @Parcelize
    data class GlobalTrackerProgram(val programUid: String) : SyncContext()

    @Parcelize
    data class TrackerProgram(val programUid: String) : SyncContext()

    @Parcelize
    data class TrackerProgramTei(val enrollmentUid: String) : SyncContext()

    @Parcelize
    data class Enrollment(val enrollmentUid: String) : SyncContext()

    @Parcelize
    data class EnrollmentEvent(val eventUid: String, val enrollmentUid: String) : SyncContext()

    @Parcelize
    data class GlobalEventProgram(val programUid: String) : SyncContext()

    @Parcelize
    data class EventProgram(val programUid: String) : SyncContext()

    @Parcelize
    data class Event(val eventUid: String) : SyncContext()

    @Parcelize
    data class GlobalDataSet(val dataSetUid: String) : SyncContext()

    @Parcelize
    data class DataSet(val dataSetUid: String) : SyncContext()

    @Parcelize
    data class DataSetInstance(
        val dataSetUid: String,
        val periodId: String,
        val orgUnitUid: String,
        val attributeOptionComboUid: String
    ) : SyncContext()

    fun conflictType() = when (this) {
        is DataSet -> ConflictType.DATA_SET
        is DataSetInstance -> ConflictType.DATA_VALUES
        is Enrollment -> ConflictType.TEI
        is EnrollmentEvent -> ConflictType.TEI
        is Event -> ConflictType.EVENT
        is EventProgram -> ConflictType.PROGRAM
        is Global -> ConflictType.ALL
        is GlobalDataSet -> ConflictType.DATA_SET
        is GlobalEventProgram -> ConflictType.PROGRAM
        is GlobalTrackerProgram -> ConflictType.PROGRAM
        is TrackerProgram -> ConflictType.PROGRAM
        is TrackerProgramTei -> ConflictType.TEI
    }

    fun recordUid() = when (this) {
        is DataSet -> dataSetUid
        is DataSetInstance -> dataSetUid
        is Enrollment -> enrollmentUid
        is EnrollmentEvent -> enrollmentUid
        is Event -> eventUid
        is EventProgram -> programUid
        is Global -> ""
        is GlobalDataSet -> dataSetUid
        is GlobalEventProgram -> programUid
        is GlobalTrackerProgram -> programUid
        is TrackerProgram -> programUid
        is TrackerProgramTei -> enrollmentUid
    }
}
