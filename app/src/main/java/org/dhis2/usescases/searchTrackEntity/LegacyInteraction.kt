package org.dhis2.usescases.searchTrackEntity

enum class LegacyInteractionID {
    ON_ENROLL_CLICK,
    ON_ADD_RELATIONSHIP,
    ON_SYNC_CLICK,
    ON_ENROLL,
    ON_TEI_CLICK,
}

sealed class LegacyInteraction(val id: LegacyInteractionID) {
    data class OnEnrollClick(val queryData: MutableMap<String, String>) :
        LegacyInteraction(LegacyInteractionID.ON_ENROLL_CLICK)

    data class OnAddRelationship(
        val teiUid: String,
        val relationshipTypeUid: String?,
        val online: Boolean,
    ) : LegacyInteraction(LegacyInteractionID.ON_ADD_RELATIONSHIP)

    data class OnSyncIconClick(val teiUid: String) :
        LegacyInteraction(LegacyInteractionID.ON_SYNC_CLICK)

    data class OnEnroll(
        val initialProgramUid: String?,
        val teiUid: String,
        val queryData: MutableMap<String, String>,
    ) : LegacyInteraction(LegacyInteractionID.ON_ENROLL)

    data class OnTeiClick(val teiUid: String, val enrollmentUid: String?, val online: Boolean) :
        LegacyInteraction(LegacyInteractionID.ON_TEI_CLICK)
}
