package org.dhis2.maps.model

import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

data class MapItemModel(
    val uid: String,
    val avatarProviderConfiguration: AvatarProviderConfiguration,
    val title: String,
    val description: String?,
    val lastUpdated: String,
    val additionalInfoList: List<AdditionalInfoItem>,
    val isOnline: Boolean,
    val geometry: Geometry?,
    val relatedInfo: RelatedInfo?,
    val state: State,
) {
    fun profilePicturePath() =
        avatarProviderConfiguration.takeIf { it is AvatarProviderConfiguration.ProfilePic }?.let {
            (it as AvatarProviderConfiguration.ProfilePic).profilePicturePath
        }
}

data class RelatedInfo(
    val enrollment: Enrollment? = null,
    val event: Event? = null,
    val relationship: Relationship? = null,
) {
    data class Enrollment(
        val uid: String,
        val geometry: Geometry?,
    )

    data class Event(
        val stageUid: String,
        val stageDisplayName: String,
        val teiUid: String?,
    )

    data class Relationship(
        val uid: String?,
        val displayName: String,
        val relationshipTypeUid: String?,
        val relatedUid: String?,
        val relationshipDirection: RelationshipDirection?,
    )
}
