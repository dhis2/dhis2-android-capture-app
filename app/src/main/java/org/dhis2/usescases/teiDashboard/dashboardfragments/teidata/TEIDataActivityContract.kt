package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent

interface TEIDataActivityContract {
    fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String)
    fun finishActivity()
    fun getTeiDashboardMobileActivityIntent(): Intent?
    fun openSyncDialog()
    fun executeOnUIThread()
}