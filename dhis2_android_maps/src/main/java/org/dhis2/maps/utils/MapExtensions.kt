package org.dhis2.maps.utils

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs

fun Style.updateSource(sourceId: String, feature: Feature) {
    getSourceAs<GeoJsonSource>(sourceId)?.apply {
        feature(feature)
    } ?: addSource(
        GeoJsonSource.Builder(sourceId)
            .feature(feature)
            .build()
    )
}

fun Style.updateSource(sourceId: String, featureCollection: FeatureCollection) {
    getSourceAs<GeoJsonSource>(sourceId)?.apply {
        featureCollection(featureCollection)
    } ?: addSource(
        GeoJsonSource.Builder(sourceId)
            .featureCollection(featureCollection)
            .build()
    )
}
