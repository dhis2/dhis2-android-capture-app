package org.dhis2.maps.managers

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.maps.R
import org.hisp.dhis.android.core.common.FeatureType

class DefaultMapManager(
    mapView: MapView,
    private val featureType: FeatureType,
) : MapManager(mapView) {

    private var featureCollection: FeatureCollection? = null

    companion object {
        const val LAYER_ID = "selected_features_layer"
        const val FILL_LAYER_ID = "fill_layer"
        const val SOURCE_ID = "selected_features"
        const val POINT_ICON_ID = "POINT_ICON_ID"
        const val POLYGON_ICON_ID = "POLYGON_ICON_ID"
    }

    fun update(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox,
    ) {
        this.featureCollection = featureCollection
        initCameraPosition(boundingBox)
        setSource()
        setLayer()
    }

    override fun loadDataForStyle() {
        if (featureType == FeatureType.POINT) {
            style?.addImage(
                POINT_ICON_ID,
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.maplibre_marker_icon_default,
                )!!,
            )
        }

        if (featureType == FeatureType.POLYGON) {
            style?.addImage(
                POLYGON_ICON_ID,
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_oval_green,
                )!!,
            )
        }

        setLayer()
    }

    override fun setSource() {
        (style?.getSource(SOURCE_ID) as GeoJsonSource?)?.setGeoJson(featureCollection)
            ?: style?.addSource(GeoJsonSource(SOURCE_ID, featureCollection))
    }

    override fun setLayer() {
        if (featureType == FeatureType.POINT && style?.getLayer(LAYER_ID) == null) {
            style?.addLayer(
                SymbolLayer(LAYER_ID, SOURCE_ID)
                    .withProperties(
                        PropertyFactory.iconImage(POINT_ICON_ID),
                    ),
            )
        }

        if (featureType == FeatureType.POLYGON && style?.getLayer(LAYER_ID) == null) {
            style?.addLayer(
                SymbolLayer(LAYER_ID, SOURCE_ID)
                    .withProperties(
                        PropertyFactory.iconImage(POLYGON_ICON_ID),
                    ),
            )

            style?.addLayerBelow(
                FillLayer(FILL_LAYER_ID, SOURCE_ID)
                    .withProperties(
                        fillColor(
                            ContextCompat.getColor(
                                mapView.context,
                                R.color.green_7ed,
                            ),
                        ),
                    ),
                "settlement-label",
            )
        }
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): Feature? {
        return null
    }

    override fun findFeature(propertyValue: String): Feature? {
        return null
    }
}
