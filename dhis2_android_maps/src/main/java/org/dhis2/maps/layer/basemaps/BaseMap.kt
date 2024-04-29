package org.dhis2.maps.layer.basemaps

import android.graphics.drawable.Drawable

data class BaseMap(
    val baseMapStyle: BaseMapStyle,
    val basemapName: String,
    val basemapImage: Drawable?,
)
