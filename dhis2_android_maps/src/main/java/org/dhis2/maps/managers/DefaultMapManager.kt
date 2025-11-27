package org.dhis2.maps.managers

import android.graphics.RectF
import androidx.appcompat.content.res.AppCompatResources
import org.dhis2.commons.bindings.dp
import org.dhis2.maps.R
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.types.PLACES_LAYER_ID
import org.dhis2.maps.layer.types.PLACES_SOURCE_ID
import org.dhis2.maps.layer.types.PlacesMapLayer
import org.hisp.dhis.android.core.common.FeatureType
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.BoundingBox
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

class DefaultMapManager(
    mapView: MapView,
    locationEngine: LocationEngine,
    private val featureType: FeatureType,
) : MapManager(mapView, locationEngine) {
    private var featureCollection: FeatureCollection? = null
    private var boundingBox: BoundingBox? = null
    override var numberOfUiIcons = 1
    override var defaultUiIconRightMargin = 12.dp

    companion object {
        const val LAYER_ID = "selected_features_layer"
        const val FILL_LAYER_ID = "fill_layer"
        const val SOURCE_ID = "selected_features"
        const val POINT_ICON_ID = "POINT_ICON_ID"
        const val POLYGON_ICON_ID = "POLYGON_ICON_ID"
    }

    fun update(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox?,
    ) {
        this.featureCollection = featureCollection
        this.boundingBox = boundingBox
        boundingBox?.let { initCameraPosition(it) }
        setSource()
        setIcons()
        setLayer()
    }

    fun updateCameraPosition() {
        boundingBox?.let { initCameraPosition(it) }
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
        (style?.getSource(PLACES_SOURCE_ID) as GeoJsonSource?)?.setGeoJson(featureCollection)
            ?: style?.addSource(GeoJsonSource(PLACES_SOURCE_ID, featureCollection))
    }

    private fun setIcons() {
        AppCompatResources
            .getDrawable(
                mapView.context,
                R.drawable.ic_map_pin,
            )?.let { placeIconDrawable ->
                style?.addImage(
                    MapLayerManager.PLACE_ICON_ID,
                    placeIconDrawable,
                )
            }
    }

    override fun setLayer() {
        if (style?.getLayer(PLACES_LAYER_ID) == null) {
            style?.let {
                mapLayerManager.mapLayers[PLACES_SOURCE_ID] = PlacesMapLayer(it)
            }
        }
    }

    override fun markFeatureAsSelected(
        point: LatLng,
        layer: String?,
    ): Feature? {
        val rectF =
            map?.projection?.toScreenLocation(point)?.let { pointf ->
                RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)
            } ?: RectF()
        var selectedFeature: Feature? = null
        val features = map?.queryRenderedFeatures(rectF, PLACES_LAYER_ID) ?: emptyList()
        if (features.isNotEmpty()) {
            mapLayerManager.selectFeature(null)
            selectedFeature = features.first()
        }

        featureCollection?.features()?.forEach {
            if (it.getStringProperty("id") == selectedFeature?.getStringProperty("id")) {
                it.addBooleanProperty("selected", true)
            } else {
                it.addBooleanProperty("selected", false)
            }
        }

        setSource()

        return selectedFeature
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): Feature? = null

    override fun findFeature(propertyValue: String): Feature? = null
}
