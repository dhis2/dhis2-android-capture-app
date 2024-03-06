package org.dhis2.utils.granularsync

import androidx.fragment.app.FragmentActivity
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.OnSyncNavigationListener
import org.dhis2.commons.sync.SyncComponentProvider
import org.dhis2.commons.sync.SyncContext

class SyncStatusDialogProvider : SyncComponentProvider {

    override fun showSyncStatusDialog(
        activity: FragmentActivity,
        syncContext: SyncContext,
        dismissListener: OnDismissListener?,
        onSyncNavigationListener: OnSyncNavigationListener?
    ) {
        val syncBuilder = SyncStatusDialog.Builder()
            .withContext(activity, onSyncNavigationListener)
            .withSyncContext(syncContext)

        with(syncBuilder) {
            dismissListener?.let { onDismissListener(it) }
        }
        syncBuilder
            .show(syncContext.conflictType().name)
    }
}
