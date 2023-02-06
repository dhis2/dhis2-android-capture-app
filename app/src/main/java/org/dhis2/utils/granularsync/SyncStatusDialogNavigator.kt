package org.dhis2.utils.granularsync

import android.content.Context
import android.content.Intent
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.EventMode

const val LAUNCH_SYNC_DIALOG = "LAUNCH_SYNC_DIALOG"

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
        }.launchSyncDialog()
        context.startActivity(intent)
    }

    private fun navigateToSearchScreen(trackerProgramSyncItem: SyncStatusType.TrackerProgram): Intent {
        return SearchTEActivity.intent(
            context,
            trackerProgramSyncItem.programUid
        )
    }

    private fun navigateToTeiDashboard(teiSyncType: SyncStatusType.TrackedEntity): Intent {
        return TeiDashboardMobileActivity.intent(
            context,
            teiSyncType.teiUid,
            teiSyncType.programUid,
            teiSyncType.enrollmentUid
        )
    }

    private fun navigateToEventProgram(eventProgramSyncItem: SyncStatusType.EventProgram): Intent {
        return ProgramEventDetailActivity.intent(
            context,
            eventProgramSyncItem.programUid
        )
    }

    private fun navigateToEvent(eventSyncItem: SyncStatusType.Event): Intent {
        return EventCaptureActivity.intent(
            context,
            eventSyncItem.eventUid,
            eventSyncItem.programUid,
            EventMode.CHECK
        )
    }

    private fun navigateToDataSetInstanceTable(tableSyncItem: SyncStatusType.DataSetInstance): Intent {
        return DataSetTableActivity.intent(
            context,
            tableSyncItem.dataSetUid,
            tableSyncItem.orgUnitUid,
            tableSyncItem.periodId,
            tableSyncItem.attrOptComboUid
        )
    }

    private fun navigateToDataSetInstances(dataSetSyncItem: SyncStatusType.DataSet): Intent {
        return DataSetDetailActivity.intent(
            context,
            dataSetSyncItem.dataSetUid
        )
    }

    private fun Intent.launchSyncDialog(): Intent {
        this.putExtra(LAUNCH_SYNC_DIALOG, true)
        return this
    }
}

fun Intent.shouldLaunchSyncDialog(): Boolean =
    getBooleanExtra(LAUNCH_SYNC_DIALOG, false)
