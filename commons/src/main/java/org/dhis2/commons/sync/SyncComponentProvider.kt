package org.dhis2.commons.sync

import androidx.fragment.app.FragmentActivity

interface SyncComponentProvider {
    fun showSyncStatusDialog(
        activity: FragmentActivity,
        syncContext: SyncContext,
        dismissListener: OnDismissListener? = null,
        onSyncNavigationListener: OnSyncNavigationListener? = null
    )
}
