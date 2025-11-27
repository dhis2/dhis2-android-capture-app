package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Context

interface TEIDataActivityContract {
    fun restoreAdapter(
        programUid: String,
        teiUid: String,
        enrollmentUid: String,
    )

    fun finishActivity()

    fun openSyncDialog()

    fun executeOnUIThread()

    fun getContext(): Context

    fun activityTeiUid(): String?
}
