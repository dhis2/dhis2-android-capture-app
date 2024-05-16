package org.dhis2.commons.sync

import android.content.Intent

interface OnSyncNavigationListener {
    fun intercept(syncStatusItem: SyncStatusItem, intent: Intent): Intent?
}
