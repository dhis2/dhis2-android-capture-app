package org.dhis2.uicomponents.map.managers

import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.HashMap
import org.dhis2.R
import org.dhis2.uicomponents.map.layer.LayerType
import org.hisp.dhis.android.core.common.FeatureType

class RelationshipMapManager : MapManager() {

    companion object {
        const val RELATIONSHIP_ICON = "RELATIONSHIP_ICON"
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
        if(isMapReady()) {
            loadDataForStyle()
        }
    }

    override fun loadDataForStyle() {
        style?.addImage(
            RELATIONSHIP_ICON,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.map_marker
            )!!
        )
        setSource()
        setLayer()
        initCameraPosition(boundingBox)
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
