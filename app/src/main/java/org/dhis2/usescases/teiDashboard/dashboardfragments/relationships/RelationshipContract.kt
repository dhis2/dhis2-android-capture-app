package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity

const val TEI_A_UID = "TEI_A_UID"

class AddRelationshipContract : ActivityResultContract<RelationshipInput, RelationshipResult>() {
    override fun createIntent(context: Context, input: RelationshipInput): Intent {
        return SearchTEActivity.getIntent(
            context,
            null,
            input.teiTypeToAdd,
            input.teiUid,
            true
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): RelationshipResult {
        return intent?.getStringExtra(TEI_A_UID)?.let { RelationshipResult.Success(it) }
            ?: RelationshipResult.Error()
    }
}

data class RelationshipInput(val teiUid: String, val teiTypeToAdd: String)
sealed class RelationshipResult() {
    data class Success(val teiUidToAddAsRelationship: String) : RelationshipResult()
    data class Error(val message: String = "") : RelationshipResult()
}
