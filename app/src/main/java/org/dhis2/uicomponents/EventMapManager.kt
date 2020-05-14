package org.dhis2.uicomponents

import android.graphics.BitmapFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.maps.MapLayerManager
import org.hisp.dhis.android.core.common.FeatureType

class EventMapManager(
    override var mapView: MapView,
    private val featureCollection: FeatureCollection,
    private val boundingBox: BoundingBox,
    private val featureType: FeatureType
) : MapManager() {

    companion object {
        const val ICON_ID = "ICON_ID"
        const val EVENTS = "events"
    }

    override fun init() {
        if (map == null) {
            mapView.getMapAsync {
                map = it
                map?.setStyle(
                    Style.MAPBOX_STREETS
                ) { style: Style ->
                    style.addImage(
                        ICON_ID,
                        BitmapFactory.decodeResource(
                            mapView.resources,
                            R.drawable.mapbox_marker_icon_default
                        )
                    )
                    setSource(style)
                    setLayer(style)
                    setSymbolManager(style, featureCollection)
                }
                onMapClickListener?.let { map?.addOnMapClickListener(it) }
                initCameraPosition(boundingBox)
                markerViewManager = MarkerViewManager(mapView, map)
            }
        } else {
            (map?.style?.getSource(EVENTS) as GeoJsonSource?)?.setGeoJson(featureCollection)
            initCameraPosition(boundingBox)
        }
    }

    override fun setSource(style: Style) {
        style.addSource(GeoJsonSource(EVENTS, featureCollection))
    }

    override fun setLayer(style: Style) {
        val symbolLayer = SymbolLayer(MapLayerManager.POINT_LAYER_ID, EVENTS)
            .withProperties(
                PropertyFactory.iconImage(ICON_ID),
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
        if (featureType != FeatureType.POINT) {
            style.addLayerBelow(
                FillLayer(MapLayerManager.POLYGON_LAYER_ID, EVENTS)
                    .withProperties(
                        PropertyFactory.fillColor(
                            ColorUtils.getPrimaryColorWithAlpha(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY_LIGHT,
                                150f
                            )
                        )
                    ),
                "settlement-label"
            )
        }
    }
}
