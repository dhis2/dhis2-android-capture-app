package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.FeatureCollection

fun HashMap<String, FeatureCollection>.appendFeatureCollection(
    collectionToAppend: Map<String, FeatureCollection>
): HashMap<String, FeatureCollection> {
    this.putAll(collectionToAppend)
    return this
}
