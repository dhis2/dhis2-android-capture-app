package org.dhis2.uicomponents.map.layer

import com.mapbox.geojson.Feature

interface MapLayer {

    fun showLayer()

    fun hideLayer()

    fun setSelectedItem(feature: Feature?)

    fun findFeatureWithUid(featureUidProperty: String): Feature?

}
