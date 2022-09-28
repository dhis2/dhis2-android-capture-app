package org.dhis2.maps.managers

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.ScreenCoordinate
import org.dhis2.maps.R
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.FIELD_NAME
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.LayerType
import org.dhis2.maps.utils.updateSource
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
            )?.toBitmap()!!
        )
        setLayer()
        addDynamicIcons()
        addDynamicLayers()
    }

    override fun setSource() {
        style?.updateSource(EVENTS, featureCollection!!)

        deFeatureCollection.forEach {
            style?.updateSource(it.key, it.value)
        }
    }

    override fun setLayer() {
        if (featureType != null && featureType != FeatureType.NONE) {
            mapLayerManager
                .addStartLayer(LayerType.EVENT_LAYER, featureType)
        }
    }

    private fun addDynamicIcons() {
        deFeatureCollection.entries.forEach {
            style?.addImage(
                "${DE_ICON_ID}_${it.key}",
                getTintedDrawable(it.key).toBitmap()
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
        return if (source == EVENTS) {
            featureCollection?.features()?.firstOrNull {
                it.getStringProperty(propertyName) == propertyValue
            }
        } else {
            deFeatureCollection[source]?.features()?.firstOrNull {
                it.getStringProperty(propertyName) == propertyValue
            }
        }
    }

    override fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String
    ): List<Feature>? {
        return mutableListOf<Feature>().apply {
            featureCollection?.features()?.filter {
                mapLayerManager.getLayer(LayerType.EVENT_LAYER.name)?.visible == true &&
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
            val feature = findFeature(EVENTS, propertyLabel, propertyValue)
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

    override fun markFeatureAsSelected(
        point: Point,
        layer: String?,
        onFeature: (Feature?) -> Unit
    ) {
        val screenCoordinate = map?.pixelForCoordinate(point)
        if (screenCoordinate != null) {
            selectedFeature(screenCoordinate) {
                onFeature(it)
            }
        } else {
            onFeature(null)
        }
    }

    private fun selectedFeature(
        screenCoordinate: ScreenCoordinate,
        onFeatureClicked: (Feature?) -> Unit
    ) {
        var selectedFeature: Feature? = null
        mapLayerManager.sourcesAndLayersForSearch()
            .filter { it.value.isNotEmpty() }
            .forEach { (source, layer) ->
                map?.queryRenderedFeatures(
                    RenderedQueryGeometry(screenCoordinate),
                    RenderedQueryOptions(listOf(layer.first()), null)
                ) { expected ->
                    if (expected.isValue && expected.value?.size!! > 0) {
                        expected.value?.get(0)?.feature?.let { feature ->
                            if (selectedFeature == null) {
                                mapLayerManager.selectFeature(null)
                            }
                            if (selectedFeature == null || source.contains(TeiMapManager.TEIS_SOURCE_ID)) {
                                selectedFeature = when {
                                    layer.any { it.contains("RELATIONSHIP") } -> findFeature(
                                        source,
                                        MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
                                        feature.getStringProperty(
                                            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID
                                        )
                                    )
                                    else -> feature
                                }
                            }
                        }
                    }
                    onFeatureClicked(selectedFeature)
                }
            }
    }
}
