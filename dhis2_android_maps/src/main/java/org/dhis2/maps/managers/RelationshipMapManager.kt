package org.dhis2.maps.managers

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.MapView
import org.dhis2.maps.R
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.LayerType
import org.dhis2.maps.utils.updateSource

class RelationshipMapManager(mapView: MapView) : MapManager(mapView) {

    companion object {
        const val RELATIONSHIP_ICON = "RELATIONSHIP_ICON"
        const val RELATIONSHIP_ARROW = "RELATIONSHIP_ARROW"
        const val RELATIONSHIP_ARROW_BIDIRECTIONAL = "RELATIONSHIP_ARROW_BIDIRECTIONAL"
    }

    private lateinit var boundingBox: BoundingBox
    private lateinit var featureCollections: Map<String, FeatureCollection>

    fun update(
        featureCollections: Map<String, FeatureCollection>,
        boundingBox: BoundingBox
    ) {
        this.featureCollections = featureCollections
        this.boundingBox = boundingBox
        if (isMapReady()) {
            when {
                mapLayerManager.mapLayers.isNotEmpty() -> updateStyleSources()
                else -> loadDataForStyle()
            }
        }
    }

    override fun loadDataForStyle() {
        style?.addImage(
            RELATIONSHIP_ARROW,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.ic_arrowhead
            )?.toBitmap()!!,
            true
        )
        style?.addImage(
            RELATIONSHIP_ICON,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.map_marker
            )?.toBitmap()!!,
            true
        )
        style?.addImage(
            RELATIONSHIP_ARROW_BIDIRECTIONAL,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.ic_arrowhead_bidirectional
            )?.toBitmap()!!,
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
        featureCollections.keys.forEach { sourceId ->
            style?.updateSource(sourceId, featureCollections[sourceId]!!)
        }
        initCameraPosition(boundingBox)
    }

    override fun setLayer() {
        mapLayerManager
            .addLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList(), true)
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return featureCollections[source]?.features()?.firstOrNull {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeature(propertyValue: String): Feature? {
        val mainProperties = arrayListOf(
            MapTeisToFeatureCollection.TEI_UID,
            MapTeisToFeatureCollection.ENROLLMENT_UID,
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT
        )
        var featureToReturn: Feature? = null
        for (source in featureCollections.keys) {
            for (propertyLabel in mainProperties) {
                val feature = findFeature(source, propertyLabel, propertyValue)
                if (feature != null) {
                    featureToReturn = feature
                    mapLayerManager.getLayer(source, true)?.setSelectedItem(featureToReturn)
                    break
                }
                if (featureToReturn != null) {
                    break
                }
            }
        }
        return featureToReturn
    }
}
