package org.dhis2.uicomponents.map.managers

import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.dhis2.R
import org.dhis2.uicomponents.map.layer.LayerType
import org.hisp.dhis.android.core.common.FeatureType

class RelationshipMapManager : MapManager() {

    companion object {
        const val RELATIONSHIP_ICON = "RELATIONSHIP_ICON"
        const val RELATIONSHIP_ARROW = "RELATIONSHIP_ARROW"
        const val RELATIONSHIP_ARROW_BIDIRECTIONAL = "RELATIONSHIP_ARROW_BIDIRECTIONAL"
    }

    private lateinit var boundingBox: BoundingBox
    private lateinit var featureCollections: Map<String, FeatureCollection>

    fun update(
        featureCollections: Map<String, FeatureCollection>,
        boundingBox: BoundingBox,
        featureType: FeatureType
    ) {
        this.featureCollections = featureCollections
        this.featureType = featureType
        this.boundingBox = boundingBox
        if (isMapReady()) {
            when {
                mapLayerManager.mapLayers.isNotEmpty() -> updateStyleSources()
                else -> loadDataForStyle()
            }
            initCameraPosition(boundingBox)
        }
    }

    override fun loadDataForStyle() {
        style?.addImage(
            RELATIONSHIP_ARROW,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead
                )
            )!!,
            true
        )
        style?.addImage(
            RELATIONSHIP_ICON,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.map_marker
                )
            )!!,
            true
        )
        style?.addImage(
            RELATIONSHIP_ARROW_BIDIRECTIONAL,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead_bidirectional
                )
            )!!,
            true
        )
        setSource()
        setLayer()
    }

    private fun updateStyleSources() {
        setSource()
        mapLayerManager.updateLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList())
    }

    override fun setSource() {
        featureCollections.keys.forEach {
            style?.getSourceAs<GeoJsonSource>(it)?.setGeoJson(featureCollections[it])
                ?: style?.addSource(GeoJsonSource(it, featureCollections[it]))
        }
    }

    override fun setLayer() {
        mapLayerManager.initMap(map)
            .withFeatureType(featureType)
            .addLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList(), true)
    }
}
