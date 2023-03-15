package org.dhis2.utils.granularsync

import android.content.Context
import android.content.Intent
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.EventMode

const val LAUNCH_SYNC_DIALOG = "LAUNCH_SYNC_DIALOG"
const val OPEN_ERROR_LOCATION = "OPEN_ERROR_LOCATION"

class SyncStatusDialogNavigator(
    private val context: Context
) {
    fun navigateTo(syncStatusItem: SyncStatusItem) {
        val intent = when (syncStatusItem.type) {
            is SyncStatusType.DataSet -> navigateToDataSetInstances(syncStatusItem.type)
            is SyncStatusType.DataSetInstance -> navigateToDataSetInstanceTable(syncStatusItem.type)
            is SyncStatusType.Event -> navigateToEvent(syncStatusItem.type)
            is SyncStatusType.EventProgram -> navigateToEventProgram(syncStatusItem.type)
            is SyncStatusType.TrackedEntity -> navigateToTeiDashboard(syncStatusItem.type)
            is SyncStatusType.TrackerProgram -> navigateToSearchScreen(syncStatusItem.type)
            is SyncStatusType.Enrollment -> navigateToEnrollmentFormScreen(syncStatusItem.type)
        }
        intent?.let { context.startActivity(it) }
    }

    private fun navigateToEnrollmentFormScreen(
        enrollmentSyncItem: SyncStatusType.Enrollment
    ): Intent? {
        return if (context !is EnrollmentActivity) {
            EnrollmentActivity.getIntent(
                context,
                enrollmentSyncItem.enrollmentUid,
                enrollmentSyncItem.programUid,
                EnrollmentActivity.EnrollmentMode.CHECK
            )
                .openErrorLocation()
        } else {
            null
        }
    }

    private fun navigateToSearchScreen(
        trackerProgramSyncItem: SyncStatusType.TrackerProgram
    ): Intent {
        return SearchTEActivity.getIntent(
            context,
            trackerProgramSyncItem.programUid,
            trackerProgramSyncItem.trackedEntityTypeUid,
            null,
            false
        ).launchSyncDialog()
    }

    private fun navigateToTeiDashboard(teiSyncType: SyncStatusType.TrackedEntity): Intent? {
        return if (context !is TeiDashboardMobileActivity) {
            return TeiDashboardMobileActivity.intent(
                context,
                teiSyncType.teiUid,
                teiSyncType.programUid,
                teiSyncType.enrollmentUid
            ).launchSyncDialog()
        } else {
            null
        }
    }

    private fun navigateToEventProgram(eventProgramSyncItem: SyncStatusType.EventProgram): Intent {
        return ProgramEventDetailActivity.intent(
            context,
            eventProgramSyncItem.programUid
        ).launchSyncDialog()
    }

    private fun navigateToEvent(eventSyncItem: SyncStatusType.Event): Intent? {
        return if (context !is EventCaptureActivity) {
            val intent = EventCaptureActivity.intent(
                context,
                eventSyncItem.eventUid,
                eventSyncItem.programUid,
                eventSyncItem.hasNullDataElementConflict,
                EventMode.CHECK
            )
            if (eventSyncItem.hasNullDataElementConflict) {
                intent.launchSyncDialog()
            } else {
                intent.openErrorLocation()
            }
        } else {
            if (eventSyncItem.hasNullDataElementConflict) {
                context.openDetails()
            } else {
                context.openForm()
            }
            null
        }
    }

    private fun navigateToDataSetInstanceTable(
        tableSyncItem: SyncStatusType.DataSetInstance
    ): Intent? {
        return if (context !is DataSetTableActivity) {
            DataSetTableActivity.intent(
                context,
                tableSyncItem.dataSetUid,
                tableSyncItem.orgUnitUid,
                tableSyncItem.periodId,
                tableSyncItem.attrOptComboUid
            ).openErrorLocation()
        } else {
            null
        }
    }

    private fun navigateToDataSetInstances(dataSetSyncItem: SyncStatusType.DataSet): Intent {
        return DataSetDetailActivity.intent(
            context,
            dataSetSyncItem.dataSetUid
        ).launchSyncDialog()
    }

    private fun Intent.launchSyncDialog(): Intent {
        this.putExtra(LAUNCH_SYNC_DIALOG, true)
        return this
    }

    private fun Intent.openErrorLocation(): Intent {
        this.putExtra(OPEN_ERROR_LOCATION, true)
        return this
    }
}

fun Intent.shouldLaunchSyncDialog(): Boolean =
    getBooleanExtra(LAUNCH_SYNC_DIALOG, false)
