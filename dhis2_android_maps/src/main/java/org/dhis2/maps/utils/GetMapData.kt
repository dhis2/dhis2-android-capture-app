package org.dhis2.maps.utils

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.dhis2.maps.extensions.withPlacesProperties
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.getLatLngPointList
import org.dhis2.maps.geometry.getPolygonPoints
import org.dhis2.maps.model.MapData
import org.dhis2.maps.views.MapSelectorViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

object GetMapData {

    private val getBoundingBox = GetBoundingBox()

    operator fun invoke(
        currentFeature: Feature?,
        locationItems: List<LocationItemModel>,
        captureMode: MapSelectorViewModel.CaptureMode,
        searchRegion: LatLngBounds? = null,
    ): MapData {
        val selectedFeatures = buildList {
            currentFeature?.takeIf { !captureMode.isSwipe() }
                ?.let {
                    add(it)
                }
        }

        val polygonPointFeatures = currentFeature?.getPolygonPoints() ?: emptyList()
        polygonPointFeatures.forEach { feature ->
            feature.withPlacesProperties()
        }

        val visibleRegion = buildList {
            searchRegion?.let {
                listOf(
                    Pair(it.northWest, it.northEast),
                    Pair(it.northEast, it.southEast),
                    Pair(it.southEast, it.southWest),
                    Pair(it.southWest, it.northWest),
                ).forEach { (first, second) ->
                    add(
                        Feature.fromGeometry(
                            LineString.fromLngLats(
                                listOf(
                                    Point.fromLngLat(first.longitude, first.latitude),
                                    Point.fromLngLat(second.longitude, second.latitude),
                                ),
                            ),
                        ),
                    )
                }
            }
        }

        val locationFeatures = locationItems
            .takeIf { captureMode.isSearch() }
            ?.map {
                Feature.fromGeometry(
                    Point.fromLngLat(it.longitude, it.latitude),
                ).also { feature ->
                    feature.withPlacesProperties(title = it.title, subtitle = it.subtitle)
                }
            } ?: emptyList()
        val features = visibleRegion + selectedFeatures + polygonPointFeatures + locationFeatures
        return MapData(
            featureCollection = FeatureCollection.fromFeatures(features),
            boundingBox = getBoundingBox.getEnclosingBoundingBox(features.getLatLngPointList()),
        )
    }
}
