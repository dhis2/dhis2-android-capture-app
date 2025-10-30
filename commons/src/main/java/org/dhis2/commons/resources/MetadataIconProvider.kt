package org.dhis2.commons.resources

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.model.FILE_NOT_LOADED
import org.dhis2.mobile.commons.model.MetadataIconData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.icon.Icon
import org.hisp.dhis.mobile.ui.designsystem.component.ImageCardData
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import timber.log.Timber
import java.io.File

class MetadataIconProvider(
    private val d2: D2,
) {
    operator fun invoke(
        color: String?,
        icon: String?,
        defaultColor: Color = SurfaceColor.Primary,
    ): MetadataIconData {
        val objectStyleBuilder = ObjectStyle.builder()
        color?.let { objectStyleBuilder.color(it) }
        icon?.let { objectStyleBuilder.icon(it) }
        return invoke(objectStyleBuilder.build(), defaultColor)
    }

    operator fun invoke(style: ObjectStyle) = invoke(style, SurfaceColor.Primary)

    operator fun invoke(
        style: ObjectStyle,
        defaultColor: Color? = SurfaceColor.Primary,
    ) = style
        .icon()
        ?.let {
            d2
                .iconModule()
                .icons()
                .key(it)
                .blockingGet()
        }.let { icon ->
            val imageCardData =
                when {
                    (icon is Icon.Custom) -> {
                        provideImageBitmap(icon.path)?.let { imageBitmap ->
                            ImageCardData.CustomIconData(
                                uid = "",
                                label = "",
                                image = imageBitmap,
                            )
                        } ?: ImageCardData.IconCardData(
                            uid = "",
                            label = "",
                            iconRes = FILE_NOT_LOADED,
                            iconTint = style.color()?.toColor() ?: defaultColor ?: Color.Unspecified,
                        )
                    }

                    else ->
                        ImageCardData.IconCardData(
                            uid = "",
                            label = "",
                            iconRes = provideIconRes(style.icon()),
                            iconTint = style.color()?.toColor() ?: Color.Unspecified,
                        )
                }
            MetadataIconData(
                imageCardData = imageCardData,
                color = style.color()?.toColor() ?: defaultColor ?: Color.Unspecified,
            )
        }

    private fun provideIconRes(icon: String?): String =
        when {
            icon != null && icon.startsWith("dhis2_") -> icon
            icon != null -> "dhis2_$icon"
            else -> ""
        }

    private fun provideImageBitmap(path: String?): ImageBitmap? {
        if (path.isNullOrEmpty()) return null
        return try {
            val file = File(path)
            BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
        } catch (e: Exception) {
            Timber.d(e)
            null
        }
    }
}
