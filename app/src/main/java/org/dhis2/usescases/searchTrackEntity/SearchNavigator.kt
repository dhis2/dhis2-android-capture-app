package org.dhis2.usescases.searchTrackEntity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import org.dhis2.commons.filters.FilterManager
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity

class SearchNavigator(
    val activity: SearchTEActivity,
    private val searchNavigationConfiguration: SearchNavigationConfiguration
) {

    private val dashboardLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (searchNavigationConfiguration.refreshDataOnBackFromDashboard()) {
            activity.refreshData()
        }
    }

    private val enrollmentLauncher = activity.registerForActivityResult(EnrollmentContract()) {
        when (it) {
            is EnrollmentResult.RelationshipResult -> {
                activity.setResult(Activity.RESULT_OK, it.data())
                activity.finish()
            }
            is EnrollmentResult.Success ->
                if (searchNavigationConfiguration.refreshDataOnBackFromEnrollment()) {
                    activity.refreshData()
                }
        }
    }

    fun changeProgram(
        programUid: String?,
        currentQueryData: Map<String, String>,
        fromRelationshipTeiUid: String?
    ) {
        val intent = Intent(activity, SearchTEActivity::class.java).apply {
            fromRelationshipTeiUid?.let { addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT) }
            putExtras(updateBundle(programUid, currentQueryData))
        }

        activity.apply {
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    fun openDashboard(teiUid: String?, programUid: String?, enrollmentUid: String?) {
        teiUid?.let { searchNavigationConfiguration.openingTEI(it) }
        FilterManager.getInstance().clearWorkingList(true)
        dashboardLauncher.launch(
            TeiDashboardMobileActivity.intent(
                activity,
                teiUid,
                if (enrollmentUid != null) programUid else null,
                enrollmentUid
            )
        )
    }

    fun goToEnrollment(enrollmentUid: String, programUid: String, fromRelationshipTeiUid: String?) {
        searchNavigationConfiguration.openingEnrollmentForm(enrollmentUid)
        enrollmentLauncher.launch(
            EnrollmentInput(
                enrollmentUid,
                programUid,
                EnrollmentActivity.EnrollmentMode.NEW,
                fromRelationshipTeiUid != null
            )
        )
    }

    private fun updateBundle(programUid: String?, currentQueryData: Map<String, String>): Bundle {
        return activity.intent.extras?.apply {
            putString(SearchTEActivity.Extra.PROGRAM_UID.key(), programUid)
            putStringArrayList(
                SearchTEActivity.Extra.QUERY_ATTR.key(),
                ArrayList(currentQueryData.keys)
            )
            putStringArrayList(
                SearchTEActivity.Extra.QUERY_VALUES.key(),
                ArrayList(currentQueryData.values)
            )
        } ?: Bundle()
    }
}
