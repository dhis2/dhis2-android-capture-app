package org.dhis2.maps.layer.basemaps

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BaseMap(
    val baseMapStyle: BaseMapStyle,
    @StringRes val basemapName: Int,
    @DrawableRes val basemapImage: Int,
)
