package org.dhis2.maps.utils

import org.dhis2.maps.extensions.withPlacesProperties
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.getLatLngPointList
import org.dhis2.maps.geometry.getPolygonPoints
import org.dhis2.maps.model.MapData
import org.dhis2.maps.views.MapSelectorViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

object GetMapData {
    private val getBoundingBox = GetBoundingBox()

    operator fun invoke(
        currentFeature: Feature?,
        locationItems: List<LocationItemModel>,
        captureMode: MapSelectorViewModel.CaptureMode,
    ): MapData {
        val selectedFeatures =
            buildList {
                currentFeature
                    ?.takeIf { !captureMode.isSwipe() }
                    ?.let {
                        add(it.withPlacesProperties(selected = true))
                    }
            }

        val polygonPointFeatures = currentFeature?.getPolygonPoints() ?: emptyList()
        polygonPointFeatures.forEach { feature ->
            feature.withPlacesProperties()
        }

        val locationFeatures =
            locationItems
                .filter { it !is LocationItemModel.StoredResult }
                .takeIf { captureMode.isSearchMode() }
                ?.map { locationItem ->
                    Feature
                        .fromGeometry(
                            Point.fromLngLat(locationItem.longitude, locationItem.latitude),
                        ).also { feature ->
                            feature.withPlacesProperties(
                                title = locationItem.title,
                                subtitle = locationItem.subtitle,
                            )
                        }
                } ?: emptyList()
        val features = selectedFeatures + polygonPointFeatures + locationFeatures
        return MapData(
            featureCollection = FeatureCollection.fromFeatures(features),
            boundingBox = boundingBox(captureMode, selectedFeatures, locationFeatures),
        )
    }

    private fun boundingBox(
        captureMode: MapSelectorViewModel.CaptureMode,
        selectedFeatures: List<Feature>,
        locationFeatures: List<Feature>,
    ) = when (captureMode) {
        MapSelectorViewModel.CaptureMode.NONE -> null
        MapSelectorViewModel.CaptureMode.GPS ->
            getBoundingBox.getEnclosingBoundingBox(
                selectedFeatures.getLatLngPointList(),
            )

        MapSelectorViewModel.CaptureMode.MANUAL -> null
        MapSelectorViewModel.CaptureMode.MANUAL_SWIPE -> null
        MapSelectorViewModel.CaptureMode.SEARCH ->
            getBoundingBox.getEnclosingBoundingBox(
                (selectedFeatures + locationFeatures).getLatLngPointList(),
            )
        MapSelectorViewModel.CaptureMode.SEARCH_SWIPE -> null
        MapSelectorViewModel.CaptureMode.SEARCH_MANUAL -> null
        MapSelectorViewModel.CaptureMode.SEARCH_PIN_CLICKED -> null
    }
}
