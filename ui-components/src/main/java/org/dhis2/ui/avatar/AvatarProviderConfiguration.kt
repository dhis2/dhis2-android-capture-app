package org.dhis2.ui.avatar

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize

sealed class AvatarProviderConfiguration {
    data class ProfilePic(
        val profilePicturePath: String,
    ) : AvatarProviderConfiguration()

    data class Metadata(
        val metadataIconData: MetadataIconData,
        val size: MetadataAvatarSize = MetadataAvatarSize.S(),
    ) : AvatarProviderConfiguration()

    data class MainValueLabel(
        val firstMainValue: String,
    ) : AvatarProviderConfiguration()
}
