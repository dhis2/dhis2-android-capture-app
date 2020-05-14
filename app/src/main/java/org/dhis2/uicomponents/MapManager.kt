package org.dhis2.uicomponents

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import org.dhis2.utils.maps.initDefaultCamera

abstract class MapManager {

    open lateinit var mapView: MapView
    var map: MapboxMap? = null
    var markerViewManager: MarkerViewManager? = null
    var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null

    abstract fun init()

    abstract fun setSource(style: Style)

    abstract fun setLayer(style: Style)

    fun setSymbolManager(style: Style, featureCollection: FeatureCollection) {
        symbolManager = SymbolManager(
            mapView, map!!, style, null,
            GeoJsonOptions().withTolerance(0.4f)
        ).apply {
            iconAllowOverlap = true
            textAllowOverlap = true
            iconIgnorePlacement = true
            textIgnorePlacement = true
            symbolPlacement = "line-center"
            create(featureCollection)
        }
    }

    fun initCameraPosition(
        boundingBox: BoundingBox
    ) {
        val bounds = LatLngBounds.from(
            boundingBox.north(),
            boundingBox.east(),
            boundingBox.south(),
            boundingBox.west()
        )
        map?.initDefaultCamera(mapView.context, bounds)
    }

    fun onStart() {
        mapView.onStart()
    }

    fun onResume() {
        mapView.onResume()
    }

    fun onPause() {
        mapView.onPause()
    }

    fun onDestroy() {
        markerViewManager?.onDestroy()
        symbolManager?.onDestroy()
    }
}
