package org.dhis2.utils.granularsync

import android.content.Context
import android.content.Intent
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.commons.sync.OnSyncNavigationListener
import org.dhis2.commons.sync.SyncStatusItem
import org.dhis2.commons.sync.SyncStatusType
import org.dhis2.form.model.EventMode
import org.dhis2.usescases.datasets.dataSetTable.DataSetInstanceActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

const val LAUNCH_SYNC_DIALOG = "LAUNCH_SYNC_DIALOG"
const val OPEN_ERROR_LOCATION = "OPEN_ERROR_LOCATION"

class SyncStatusDialogNavigator(
    private val context: Context,
    private val onSyncNavigationListener: OnSyncNavigationListener?,
) {
    fun navigateTo(
        syncStatusItem: SyncStatusItem,
        onNavigation: () -> Unit,
    ) {
        val intent =
            when (syncStatusItem.type) {
                is SyncStatusType.DataSet ->
                    navigateToDataSetInstances(syncStatusItem.type as SyncStatusType.DataSet)
                is SyncStatusType.DataSetInstance ->
                    navigateToDataSetInstanceTable(
                        syncStatusItem.type as SyncStatusType.DataSetInstance,
                    )
                is SyncStatusType.Event ->
                    navigateToEvent(syncStatusItem.type as SyncStatusType.Event)
                is SyncStatusType.EventProgram ->
                    navigateToEventProgram(syncStatusItem.type as SyncStatusType.EventProgram)
                is SyncStatusType.TrackedEntity ->
                    navigateToTeiDashboard(syncStatusItem.type as SyncStatusType.TrackedEntity)
                is SyncStatusType.TrackerProgram ->
                    navigateToSearchScreen(syncStatusItem.type as SyncStatusType.TrackerProgram)
                is SyncStatusType.StockProgram ->
                    navigateToStockUsecase(syncStatusItem.type as SyncStatusType.StockProgram)
                is SyncStatusType.Enrollment ->
                    navigateToEnrollmentFormScreen(syncStatusItem.type as SyncStatusType.Enrollment)
            }

        if (intent?.hasExtra(OPEN_ERROR_LOCATION) == false) {
            onNavigation()
        }

        if (onSyncNavigationListener != null) {
            intent?.let {
                onSyncNavigationListener.intercept(syncStatusItem, it)?.let { interceptedIntent ->
                    context.startActivity(interceptedIntent)
                }
            }
        } else {
            intent?.let {
                context.startActivity(it)
            }
        }
    }

    private fun navigateToEnrollmentFormScreen(enrollmentSyncItem: SyncStatusType.Enrollment): Intent? =
        if (context !is EnrollmentActivity) {
            EnrollmentActivity
                .getIntent(
                    context,
                    enrollmentSyncItem.enrollmentUid,
                    enrollmentSyncItem.programUid,
                    EnrollmentActivity.EnrollmentMode.CHECK,
                ).openErrorLocation()
        } else {
            null
        }

    private fun navigateToSearchScreen(trackerProgramSyncItem: SyncStatusType.TrackerProgram): Intent =
        SearchTEActivity
            .getIntent(
                context,
                trackerProgramSyncItem.programUid,
                trackerProgramSyncItem.trackedEntityTypeUid,
                null,
                false,
            ).launchSyncDialog()

    private fun navigateToStockUsecase(stockProgramSyncItem: SyncStatusType.StockProgram): Intent? =
        if (context !is HomeActivity) {
            Intent(
                context,
                HomeActivity::class.java,
            ).apply {
                putExtra(
                    org.dhis2.commons.Constants.PROGRAM_UID,
                    stockProgramSyncItem.programUid,
                )
            }
        } else {
            null
        }

    private fun navigateToTeiDashboard(teiSyncType: SyncStatusType.TrackedEntity): Intent? {
        return if (context !is TeiDashboardMobileActivity) {
            return TeiDashboardMobileActivity
                .intent(
                    context,
                    teiSyncType.teiUid,
                    teiSyncType.programUid,
                    teiSyncType.enrollmentUid,
                ).launchSyncDialog()
        } else {
            null
        }
    }

    private fun navigateToEventProgram(eventProgramSyncItem: SyncStatusType.EventProgram): Intent =
        ProgramEventDetailActivity
            .intent(
                context,
                eventProgramSyncItem.programUid,
            ).launchSyncDialog()

    private fun navigateToEvent(eventSyncItem: SyncStatusType.Event): Intent? =
        if (context !is EventCaptureActivity) {
            val intent =
                EventCaptureActivity.intent(
                    context,
                    eventSyncItem.eventUid,
                    eventSyncItem.programUid,
                    EventMode.CHECK,
                )
            intent.openErrorLocation()
            if (eventSyncItem.hasNullDataElementConflict) {
                intent.launchSyncDialog()
            } else {
                intent
            }
        } else {
            if (eventSyncItem.hasNullDataElementConflict) {
                context.openDetails()
            } else {
                context.openForm()
            }
            null
        }

    private fun navigateToDataSetInstanceTable(tableSyncItem: SyncStatusType.DataSetInstance): Intent? =
        if (context !is DataSetInstanceActivity) {
            DataSetInstanceActivity.intent(
                context,
                tableSyncItem.dataSetUid,
                tableSyncItem.orgUnitUid,
                tableSyncItem.periodId,
                tableSyncItem.attrOptComboUid,
                true,
            )
        } else {
            null
        }

    private fun navigateToDataSetInstances(dataSetSyncItem: SyncStatusType.DataSet): Intent =
        DataSetDetailActivity
            .intent(
                context,
                dataSetSyncItem.dataSetUid,
            ).launchSyncDialog()

    private fun Intent.launchSyncDialog(): Intent {
        this.putExtra(LAUNCH_SYNC_DIALOG, true)
        return this
    }

    private fun Intent.openErrorLocation(): Intent {
        this.putExtra(OPEN_ERROR_LOCATION, true)
        return this
    }
}

fun Intent.shouldLaunchSyncDialog(): Boolean = getBooleanExtra(LAUNCH_SYNC_DIALOG, false)
