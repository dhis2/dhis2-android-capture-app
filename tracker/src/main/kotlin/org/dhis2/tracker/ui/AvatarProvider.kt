package org.dhis2.tracker.ui

import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.dhis2.ui.avatar.AvatarProviderConfiguration.Metadata
import org.dhis2.ui.avatar.AvatarProviderConfiguration.ProfilePic
import org.hisp.dhis.android.core.common.ObjectStyle

class AvatarProvider(
    private val metadataIconProvider: MetadataIconProvider
) {

    fun getAvatar(
        style: ObjectStyle? = null,
        profilePath: String,
        firstAttributeValue: String?,
    ): AvatarProviderConfiguration {

        return when {
            profilePath.isNotEmpty() -> {
                ProfilePic(
                    profilePicturePath = profilePath,
                )
            }

            style != null && profilePath.isEmpty() -> {
                Metadata(
                    metadataIconData = metadataIconProvider.invoke(
                        style,
                    ),
                )
            }

            else -> {
                AvatarProviderConfiguration.MainValueLabel(
                    firstMainValue = firstAttributeValue ?: "",
                )
            }
        }
    }
}
