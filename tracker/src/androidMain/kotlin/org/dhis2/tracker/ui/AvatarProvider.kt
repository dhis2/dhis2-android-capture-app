package org.dhis2.tracker.ui

import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration.Metadata
import org.dhis2.mobile.commons.model.AvatarProviderConfiguration.ProfilePic

class AvatarProvider(
    private val metadataIconProvider: MetadataIconProvider,
) {
    fun getAvatar(
        icon: String?,
        color: String?,
        profilePath: String,
        firstAttributeValue: String?,
    ): AvatarProviderConfiguration =
        when {
            profilePath.isNotEmpty() ->
                ProfilePic(profilePicturePath = profilePath)

            (icon != null || color != null) && profilePath.isEmpty() ->
                Metadata(metadataIconData = metadataIconProvider(color, icon))

            else ->
                AvatarProviderConfiguration.MainValueLabel(
                    firstMainValue = firstAttributeValue ?: "",
                )
        }
}
