package org.dhis2.usescases.searchTrackEntity

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import org.dhis2.usescases.enrollment.EnrollmentActivity

const val TEI_A_UID = "TEI_A_UID"

class EnrollmentContract : ActivityResultContract<EnrollmentInput, EnrollmentResult>() {
    override fun createIntent(context: Context, input: EnrollmentInput): Intent {
        return EnrollmentActivity.getIntent(
            context,
            input.enrollmentUid,
            input.programUid,
            input.enrollmentMode,
            input.forRelationship,
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): EnrollmentResult {
        return intent?.getStringExtra(TEI_A_UID)?.let {
            EnrollmentResult.RelationshipResult(it)
        } ?: EnrollmentResult.Success
    }
}

data class EnrollmentInput(
    val enrollmentUid: String,
    val programUid: String,
    val enrollmentMode: EnrollmentActivity.EnrollmentMode,
    val forRelationship: Boolean,
)

sealed class EnrollmentResult {
    object Success : EnrollmentResult()
    data class RelationshipResult(val teiUid: String) : EnrollmentResult() {
        fun data() = Intent().apply {
            putExtra(TEI_A_UID, teiUid)
        }
    }
}
