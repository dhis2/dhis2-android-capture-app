package org.dhis2.ui.avatar

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarSize

sealed class AvatarProviderConfiguration {
    data class ProfilePic(
        val profilePicturePath: String,
        val firstMainValue: String,
    ) : AvatarProviderConfiguration()

    data class Metadata(
        val metadataIconData: MetadataIconData,
        val size: AvatarSize = AvatarSize.Normal,
    ) : AvatarProviderConfiguration()
}
