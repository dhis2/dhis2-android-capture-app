package org.dhis2.uicomponents.map.layer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BaseMap(
    val basemapType: BaseMapType,
    @StringRes val basemapName: Int,
    @DrawableRes val basemapImage: Int
)
