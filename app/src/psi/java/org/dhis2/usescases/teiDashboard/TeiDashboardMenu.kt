package org.dhis2.usescases.teiDashboard

import android.content.Context
import androidx.core.content.ContextCompat.startActivity
import org.dhis2.R
import org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback.FeedbackActivity

fun customClick(
    itemId: Int,
    context: Context,
    programUid: String,
    enrollmentUid: String,
    teiUid: String
) {
    when (itemId) {
        R.id.feedback -> startActivity(
            context,
            FeedbackActivity.intent(context, programUid, enrollmentUid, teiUid),
            null
        )
    }
}