package org.dhis2.uicomponents

import com.mapbox.geojson.BoundingBox
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import org.dhis2.utils.maps.initDefaultCamera
import org.hisp.dhis.android.core.common.FeatureType

abstract class MapManager {

    lateinit var mapView: MapView
    var map: MapboxMap? = null
    var changingStyle = false
    var featureType: FeatureType? = null
    var mapStyle: MapStyle? = null
    var markerViewManager: MarkerViewManager? = null
    protected var symbolManager: SymbolManager? = null
    var onMapClickListener: MapboxMap.OnMapClickListener? = null

    abstract fun setStyle()

    abstract fun setSource(style: Style)

    abstract fun setLayer(style: Style)

    fun initCameraPosition(
        map: MapboxMap,
        boundingBox: BoundingBox
    ) {
        val bounds = LatLngBounds.from(
            boundingBox.north(),
            boundingBox.east(),
            boundingBox.south(),
            boundingBox.west()
        )
        map.initDefaultCamera(mapView.context, bounds)
    }

    fun onDestroy() {
        markerViewManager?.onDestroy()
        symbolManager?.onDestroy()
    }
}