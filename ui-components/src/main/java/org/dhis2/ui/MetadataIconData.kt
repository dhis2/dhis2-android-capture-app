package org.dhis2.ui

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class MetadataIconData(
    @ColorInt val programColor: Int,
    @DrawableRes val iconResource: Int,
    val sizeInDp: Int = 40
)
