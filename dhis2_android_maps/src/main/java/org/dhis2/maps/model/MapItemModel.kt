package org.dhis2.maps.model

import io.ktor.util.reflect.instanceOf
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
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
    fun isProfilePictureAvailable() = avatarProviderConfiguration.instanceOf(AvatarProviderConfiguration.ProfilePic::class)

    fun isCustomIcon() = avatarProviderConfiguration.instanceOf(AvatarProviderConfiguration.Metadata::class)

    fun profilePicturePath() =
        avatarProviderConfiguration.takeIf { it is AvatarProviderConfiguration.ProfilePic }?.let {
            (it as AvatarProviderConfiguration.ProfilePic).profilePicturePath
        }

    fun getCustomIconRes() =
        avatarProviderConfiguration.takeIf { it is AvatarProviderConfiguration.Metadata }?.let {
            (it as AvatarProviderConfiguration.Metadata).metadataIconData.getIconRes()
        }

    fun getDefaultIconRes() = "ic_default_icon"
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
