package org.dhis2.uicomponents.map.managers

import android.graphics.BitmapFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.common.FeatureType

class EventMapManager : MapManager() {

    private lateinit var featureCollection: FeatureCollection

    companion object {
        const val ICON_ID = "ICON_ID"
        const val EVENTS = "events"
    }

    fun update(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox,
        featureType: FeatureType?
    ) {
        this.featureType = featureType ?: FeatureType.POINT
        this.featureCollection = featureCollection
        (style?.getSource(EVENTS) as GeoJsonSource?)?.setGeoJson(featureCollection)
            ?: loadDataForStyle()
        initCameraPosition(boundingBox)
    }

    override fun loadDataForStyle() {
        style?.addImage(
            ICON_ID,
            BitmapFactory.decodeResource(
                mapView.resources,
                R.drawable.mapbox_marker_icon_default
            )
        )
        setSource()
        setLayer()
        setSymbolManager(featureCollection)
    }

    override fun setSource() {
        style?.addSource(GeoJsonSource(EVENTS, featureCollection))
    }

    override fun setLayer() {
        val symbolLayer = SymbolLayer(
            "POINT_LAYER",
            EVENTS
        )
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
        style?.addLayer(symbolLayer)
        if (featureType != FeatureType.POINT) {
            style?.addLayerBelow(
                FillLayer(
                    "POLYGON_LAYER",
                    EVENTS
                )
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
