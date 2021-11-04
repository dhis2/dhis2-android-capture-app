package org.dhis2.android_maps.layer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BaseMap(
    val basemapType: BaseMapType,
    @StringRes val basemapName: Int,
    @DrawableRes val basemapImage: Int
)
