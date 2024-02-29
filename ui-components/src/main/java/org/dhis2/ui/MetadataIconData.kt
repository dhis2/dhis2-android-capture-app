package org.dhis2.ui

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

sealed class MetadataIconData(open val sizeInDp: Int = 40) {
    data class Resource(
        @ColorInt val programColor: Int,
        @DrawableRes val iconResource: Int,
        override val sizeInDp: Int = 40,
    ) : MetadataIconData()

    data class Custom(
        val file: Bitmap,
        override val sizeInDp: Int = 40,
    ) : MetadataIconData()

    fun withSize(sizeInDp: Int) = when (this) {
        is Custom -> this.copy(sizeInDp = sizeInDp)
        is Resource -> this.copy(sizeInDp = sizeInDp)
    }
}
