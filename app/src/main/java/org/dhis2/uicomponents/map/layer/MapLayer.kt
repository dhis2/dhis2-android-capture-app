package org.dhis2.uicomponents.map.layer

import com.mapbox.geojson.Feature

const val TYPE = "\$type"
const val TYPE_POINT = "Point"
const val TYPE_POLYGON = "Polygon"
const val TYPE_LINE = "LineString"

interface MapLayer {

    fun showLayer()

    fun hideLayer()

    fun setSelectedItem(feature: Feature?)

    fun findFeatureWithUid(featureUidProperty: String): Feature?

    var visible: Boolean

    fun getId(): String

    fun layerIdsToSearch(): Array<String> = emptyArray()
}
