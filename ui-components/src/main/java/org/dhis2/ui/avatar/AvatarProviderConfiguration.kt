package org.dhis2.ui.avatar

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarSize

sealed class AvatarProviderConfiguration {
    data class ProfilePic(
        val profilePicturePath: String,
        @Deprecated("Use MainValueLabel data class instead") val firstMainValue: String,
    ) : AvatarProviderConfiguration()

    data class Metadata(
        val metadataIconData: MetadataIconData,
        val size: AvatarSize = AvatarSize.Normal,
    ) : AvatarProviderConfiguration()

    data class MainValueLabel(
        val firstMainValue: String,
    ) : AvatarProviderConfiguration()
}
