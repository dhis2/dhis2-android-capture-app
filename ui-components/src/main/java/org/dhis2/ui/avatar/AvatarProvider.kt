package org.dhis2.ui.avatar

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
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
        is AvatarProviderConfiguration.MainValueLabel ->
            MainValueLabelAvatar(
                avatarProviderConfiguration,
            )
    }
}

@Composable
private fun MetadataIconAvatar(
    config: AvatarProviderConfiguration.Metadata,
) {
    Avatar(
        style = AvatarStyleData.Metadata(
            imageCardData = config.metadataIconData.imageCardData,
            avatarSize = config.size,
            tintColor = config.metadataIconData.color,
        ),
    )
}

@Composable
private fun ProfilePicAvatar(
    config: AvatarProviderConfiguration.ProfilePic,
    onImageClick: (String) -> Unit,
) {
    val file = File(config.profilePicturePath)
    val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
    val painter = BitmapPainter(bitmap)

    Avatar(
        style = AvatarStyleData.Image(painter),
        onImageClick = { onImageClick(config.profilePicturePath) },
    )
}

@Composable
private fun MainValueLabelAvatar(
    config: AvatarProviderConfiguration.MainValueLabel,
) {
    Avatar(
        style = AvatarStyleData.Text(getTitleFirstLetter(config.firstMainValue)),
    )
}

private fun getTitleFirstLetter(firstMainValue: String?): String {
    return when (firstMainValue) {
        null, "-" -> "?"
        else -> firstMainValue.uppercase()
    }
}
