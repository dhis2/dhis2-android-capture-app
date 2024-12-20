package org.dhis2.ui

import androidx.compose.ui.graphics.Color
import org.hisp.dhis.mobile.ui.designsystem.component.ImageCardData

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

    fun getIconRes() = if (imageCardData is ImageCardData.IconCardData) {
        imageCardData.iconRes
    } else {
        ""
    }

    companion object {
        fun defaultIcon() = MetadataIconData(
            imageCardData = ImageCardData.IconCardData(
                "",
                "",
                "",
                Color.Unspecified,
            ),
            color = Color.Unspecified,
        )
    }
}
