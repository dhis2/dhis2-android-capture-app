package org.dhis2.uicomponents

import com.mapbox.geojson.BoundingBox

interface StyleConfigurator {
    fun setStyle(boundingBox: BoundingBox)
}