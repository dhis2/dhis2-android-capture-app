package org.dhis2.commons.ui

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import org.dhis2.commons.resources.ColorUtils

data class MetadataIconData(
    @ColorInt val programColor: Int,
    @DrawableRes val iconResource: Int,
    val sizeInDp: Int = 40
)