package org.dhis2.uicomponents.map

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import org.dhis2.uicomponents.map.camera.initCameraToViewAllElements
import org.hisp.dhis.android.core.common.FeatureType

abstract class MapManager {

    companion object {
        const val POINT_LAYER = "POINT_LAYER"
        const val POLYGON_LAYER = "POLYGON_LAYER"
        const val POLYGON_BORDER_LAYER = "POLYGON_BORDER_LAYER"
    }

    open lateinit var mapView: MapView
    open lateinit var map: MapboxMap
    open lateinit var style: Style
    open lateinit var featureType: FeatureType
    var markerViewManager: MarkerViewManager? = null
    var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null

    fun init(mapView: MapView) {
        this.mapView = mapView
        mapView.getMapAsync {
            this.map = it
            map.setStyle(
                Style.MAPBOX_STREETS
            ) { style: Style ->
                this.style = style
            }
            onMapClickListener?.let { mapClickListener ->
                map.addOnMapClickListener(mapClickListener)
            }
            markerViewManager = MarkerViewManager(mapView, map)
        }
    }

    abstract fun loadDataForStyle()
    abstract fun setSource()
    abstract fun setLayer()

    fun setSymbolManager(featureCollection: FeatureCollection) {
        symbolManager = SymbolManager(
            mapView, map, style, null,
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
        map.initCameraToViewAllElements(mapView.context, bounds)
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
