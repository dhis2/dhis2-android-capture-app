package org.dhis2.uicomponents

import android.graphics.BitmapFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.maps.initDefaultCamera
import org.hisp.dhis.android.core.common.FeatureType

class EventMapManager(mapView: MapView): MapManager(mapView) {

    fun setStyle(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox
    ) {
        if (map == null) {
            mapView.getMapAsync {
                map = it
                map?.setStyle(
                    Style.MAPBOX_STREETS
                ) { style: Style ->
                    onMapClickListener?.let { map?.addOnMapClickListener(it) }

                    style.addImage(
                        "ICON_ID",
                        BitmapFactory.decodeResource(
                            mapView.resources,
                            R.drawable.mapbox_marker_icon_default
                        )
                    )
                    setSource(style, featureCollection)
                    setEventLayer(style)

                    initCameraPosition(map!!, boundingBox)

                    markerViewManager = MarkerViewManager(mapView, map)
                    symbolManager = SymbolManager(
                        mapView, map!!, style, null,
                        GeoJsonOptions().withTolerance(0.4f)
                    )

                    symbolManager?.iconAllowOverlap = true
                    symbolManager?.textAllowOverlap = true
                    symbolManager?.create(featureCollection)
                }
            }
        } else {
            (map!!.style!!.getSource("events") as GeoJsonSource?)!!.setGeoJson(featureCollection)
            initCameraPosition(map!!, boundingBox)
        }
    }

    private fun setSource(
        style: Style,
        featureCollection: FeatureCollection
    ) {
        style.addSource(GeoJsonSource("events", featureCollection))
    }

    private fun setEventLayer(style: Style) {
        val symbolLayer = SymbolLayer("POINT_LAYER", "events")
            .withProperties(
                PropertyFactory.iconImage("ICON_ID"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconOffset(
                    arrayOf(
                        0f,
                        -9f
                    )
                )
            )
        symbolLayer.minZoom = 0f
        style.addLayer(symbolLayer)
        if (featureType != FeatureType.POINT) style.addLayerBelow(
            FillLayer("POLYGON_LAYER", "events").withProperties(
                PropertyFactory.fillColor(
                    ColorUtils.getPrimaryColorWithAlpha(
                        mapView.context,
                        ColorUtils.ColorType.PRIMARY_LIGHT,
                        150f
                    )
                )
            ), "settlement-label"
        )
    }

    private fun initCameraPosition(
        map: MapboxMap,
        bbox: BoundingBox
    ) {
        val bounds =
            LatLngBounds.from(bbox.north(), bbox.east(), bbox.south(), bbox.west())
        map.initDefaultCamera(mapView.context, bounds)
    }
}