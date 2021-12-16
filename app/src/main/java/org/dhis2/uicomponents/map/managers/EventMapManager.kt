package org.dhis2.uicomponents.map.managers

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.FIELD_NAME
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.layer.LayerType
import org.hisp.dhis.android.core.common.FeatureType

class EventMapManager(mapView: MapView) : MapManager(mapView) {

    private var featureCollection: FeatureCollection? = null
    private var deFeatureCollection: Map<String, FeatureCollection> = emptyMap()
    var featureType: FeatureType? = null

    companion object {
        const val ICON_ID = "ICON_ID"
        const val DE_ICON_ID = "DE_ICON_ID"
        const val EVENTS = "events"
    }

    fun update(
        featureCollectionMap: MutableMap<String, FeatureCollection>,
        boundingBox: BoundingBox
    ) {
        this.featureCollection = featureCollectionMap[EVENTS]
        this.deFeatureCollection = featureCollectionMap.filter { it.key != EVENTS }
        initCameraPosition(boundingBox)
        setSource()
        addDynamicIcons()
        addDynamicLayers()
    }

    override fun loadDataForStyle() {
        style?.addImage(
            ICON_ID,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.map_marker
            )!!
        )
        setLayer()
        addDynamicIcons()
        addDynamicLayers()
    }

    override fun setSource() {
        (style?.getSource(EVENTS) as GeoJsonSource?)?.setGeoJson(featureCollection)
            ?: style?.addSource(GeoJsonSource(EVENTS, featureCollection))
        deFeatureCollection.forEach {
            (style?.getSource(it.key) as GeoJsonSource?)?.setGeoJson(it.value)
                ?: style?.addSource(GeoJsonSource(it.key, it.value))
        }
    }

    override fun setLayer() {
        mapLayerManager
            .addStartLayer(LayerType.EVENT_LAYER, featureType)
    }

    private fun addDynamicIcons() {
        deFeatureCollection.entries.forEach {
            style?.addImage(
                "${DE_ICON_ID}_${it.key}",
                getTintedDrawable(it.key)
            )
        }
    }

    private fun getTintedDrawable(sourceId: String): Drawable {
        val initialDrawable = AppCompatResources.getDrawable(
            mapView.context,
            R.drawable.map_marker
        )?.mutate()
        val wrappedDrawable = DrawableCompat.wrap(initialDrawable!!)
        mapLayerManager.getNextAvailableColor(sourceId)?.let { color ->
            DrawableCompat.setTint(wrappedDrawable, color)
        }
        return wrappedDrawable
    }

    private fun addDynamicLayers() {
        mapLayerManager
            .updateLayers(
                LayerType.FIELD_COORDINATE_LAYER,
                deFeatureCollection.keys.toList()
            )
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return featureCollection?.features()?.firstOrNull() {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String
    ): List<Feature>? {
        return mutableListOf<Feature>().apply {
            featureCollection?.features()?.filter {
                it.getStringProperty(propertyName) == propertyValue
            }?.map {
                mapLayerManager.getLayer(LayerType.EVENT_LAYER.name)
                    ?.setSelectedItem(it)
                it
            }?.let { addAll(it) }
            deFeatureCollection.values.map { collection ->
                collection.features()?.filter {
                    mapLayerManager.getLayer(it.getStringProperty(FIELD_NAME))?.visible == true &&
                        it.getStringProperty(propertyName) == propertyValue
                }?.map {
                    mapLayerManager.getLayer(it.getStringProperty(FIELD_NAME))?.setSelectedItem(it)
                    it
                }?.let { addAll(it) }
            }
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
        for (propertyLabel in mainProperties) {
            val feature = findFeature(LayerType.EVENT_LAYER.name, propertyLabel, propertyValue)
            if (feature != null) {
                featureToReturn = feature
                mapLayerManager.getLayer(LayerType.EVENT_LAYER.name, true)
                    ?.setSelectedItem(featureToReturn)
                break
            }
            if (featureToReturn != null) {
                break
            }
        }
        return featureToReturn
    }

    override fun findFeatures(propertyValue: String): List<Feature>? {
        return findFeatures("", MapEventToFeatureCollection.EVENT, propertyValue)
    }
}
