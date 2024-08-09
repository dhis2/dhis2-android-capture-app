package org.dhis2.ui.avatar

import android.graphics.BitmapFactory
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyle
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatar
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataIcon
import java.io.File

@Composable
fun AvatarProvider(
    avatarProviderConfiguration: AvatarProviderConfiguration,
    onImageClick: ((String) -> Unit),
) {
    when (avatarProviderConfiguration) {
        is AvatarProviderConfiguration.ProfilePic ->
            ProfilePicAvatar(
                avatarProviderConfiguration,
                onImageClick,
            )

        is AvatarProviderConfiguration.Metadata ->
            MetadataIconAvatar(
                avatarProviderConfiguration,
            )
    }
}

@Composable
private fun MetadataIconAvatar(
    config: AvatarProviderConfiguration.Metadata,
) {
    Avatar(
        metadataAvatar = {
            MetadataAvatar(
                icon = {
                    if (config.metadataIconData.isFileLoaded()) {
                        MetadataIcon(
                            imageCardData = config.metadataIconData.imageCardData,
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.image_not_supported),
                            contentDescription = "",
                        )
                    }
                },
                iconTint = config.metadataIconData.color,
                size = config.size,
            )
        },
        style = AvatarStyle.METADATA,
    )
}

@Composable
private fun ProfilePicAvatar(
    config: AvatarProviderConfiguration.ProfilePic,
    onImageClick: (String) -> Unit,
) {
    if (config.profilePicturePath.isNotEmpty()) {
        val file = File(config.profilePicturePath)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
        val painter = BitmapPainter(bitmap)

        Avatar(
            imagePainter = painter,
            style = AvatarStyle.IMAGE,
            onImageClick = { onImageClick(config.profilePicturePath) },
        )
    } else {
        Avatar(
            textAvatar = getTitleFirstLetter(config.firstMainValue),
            imagePainter = painterResource(id = R.drawable.image_not_supported),
            style = AvatarStyle.TEXT,
        )
    }
}

private fun getTitleFirstLetter(firstMainValue: String?): String {
    return when (firstMainValue) {
        null, "-" -> "?"
        else -> firstMainValue.uppercase()
    }
}
