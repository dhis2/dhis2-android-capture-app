package org.dhis2.usescases.searchTrackEntity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import org.dhis2.usescases.enrollment.EnrollmentActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import java.util.UUID

class SearchNavigator(
    val activity: SearchTEActivity,
    private val searchNavigationConfiguration: SearchNavigationConfiguration,
) {
    private val dashboardLauncher: ActivityResultLauncher<Intent>
        get() =
            activity.registerActivityResultLauncher(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) {
                if (searchNavigationConfiguration.refreshDataOnBackFromDashboard()) {
                    activity.refreshData()
                }
                dashboardLauncher.unregister()
            }

    private val enrollmentLauncher: ActivityResultLauncher<EnrollmentInput>
        get() =
            activity.registerActivityResultLauncher(contract = EnrollmentContract()) {
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
                enrollmentLauncher.unregister()
            }

    fun changeProgram(
        programUid: String?,
        currentQueryData: Map<String, List<String>>,
        fromRelationshipTeiUid: String?,
    ) {
        val intent =
            Intent(activity, SearchTEActivity::class.java).apply {
                fromRelationshipTeiUid?.let { addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT) }
                putExtras(updateBundle(programUid, currentQueryData))
            }

        activity.apply {
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    fun openDashboard(
        teiUid: String?,
        programUid: String?,
        enrollmentUid: String?,
    ) {
        teiUid?.let { searchNavigationConfiguration.openingTEI(it) }
        dashboardLauncher.launch(
            TeiDashboardMobileActivity.intent(
                activity,
                teiUid,
                if (enrollmentUid != null) programUid else null,
                enrollmentUid,
            ),
        )
    }

    fun goToEnrollment(
        enrollmentUid: String,
        programUid: String,
        fromRelationshipTeiUid: String?,
    ) {
        searchNavigationConfiguration.openingEnrollmentForm(enrollmentUid)
        enrollmentLauncher.launch(
            EnrollmentInput(
                enrollmentUid,
                programUid,
                EnrollmentActivity.EnrollmentMode.NEW,
                fromRelationshipTeiUid != null,
            ),
        )
    }

    private fun updateBundle(
        programUid: String?,
        currentQueryData: Map<String, List<String>>,
    ): Bundle =
        activity.intent.extras?.apply {
            putString(SearchTEActivity.Extra.PROGRAM_UID.key(), programUid)
            putStringArrayList(
                SearchTEActivity.Extra.QUERY_ATTR.key(),
                ArrayList(currentQueryData.keys),
            )
            val queryDataValues = currentQueryData.values.map { it.joinToString(",") }
            if (queryDataValues.isNotEmpty()) {
                putStringArrayList(
                    SearchTEActivity.Extra.QUERY_VALUES.key(),
                    ArrayList(queryDataValues),
                )
            }
        } ?: Bundle()

    fun <I, O> ComponentActivity.registerActivityResultLauncher(
        key: String = UUID.randomUUID().toString(),
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>,
    ): ActivityResultLauncher<I> = activityResultRegistry.register(key, contract, callback)
}
