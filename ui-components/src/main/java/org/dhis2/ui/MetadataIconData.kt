package org.dhis2.ui

import androidx.compose.ui.graphics.Color
import org.hisp.dhis.mobile.ui.designsystem.component.internal.ImageCardData

const val FILE_NOT_LOADED = "FILE_NOT_LOADED"

data class MetadataIconData(
    val imageCardData: ImageCardData,
    val color: Color,
) {
    fun isFileLoaded(): Boolean {
        return when (imageCardData) {
            is ImageCardData.CustomIconData -> true
            is ImageCardData.IconCardData -> imageCardData.iconRes != FILE_NOT_LOADED
        }
    }

    companion object {
        fun defaultIcon() = MetadataIconData(
            imageCardData = ImageCardData.IconCardData(
                "",
                "",
                "ic_default_icon",
                Color.Unspecified,
            ),
            color = Color.Unspecified,
        )
    }
}
