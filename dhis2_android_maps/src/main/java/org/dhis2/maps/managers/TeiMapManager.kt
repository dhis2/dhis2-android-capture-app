package org.dhis2.maps.managers

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.ScreenCoordinate
import org.dhis2.commons.bindings.dp
import org.dhis2.maps.R
import org.dhis2.maps.TeiMarkers
import org.dhis2.maps.geometry.mapper.EventsByProgramStage
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.Companion.RELATIONSHIP_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_IMAGE
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_UID
import org.dhis2.maps.layer.LayerType
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.model.MapStyle
import org.dhis2.maps.utils.updateSource
import org.hisp.dhis.android.core.common.FeatureType

class TeiMapManager(mapView: MapView) : MapManager(mapView) {

    private var fieldFeatureCollections: Map<String, FeatureCollection> = emptyMap()
    private var teiFeatureCollections: HashMap<String, FeatureCollection>? = null
    private var eventsFeatureCollection: Map<String, FeatureCollection>? = null
    var mapStyle: MapStyle? = null
    private var teiImages: HashMap<String, Bitmap> = hashMapOf()
    var teiFeatureType: FeatureType? = FeatureType.POINT
    var enrollmentFeatureType: FeatureType? = FeatureType.POINT
    private var boundingBox: BoundingBox? = null

    init {
        numberOfUiIcons = 3
    }

    companion object {
        const val TEIS_SOURCE_ID = "TEIS_SOURCE_ID"
        const val ENROLLMENT_SOURCE_ID = "ENROLLMENT_SOURCE_ID"
    }

    fun update(
        teiFeatureCollections: HashMap<String, FeatureCollection>,
        eventsFeatureCollection: EventsByProgramStage,
        fieldFeatures: MutableMap<String, FeatureCollection>,
        boundingBox: BoundingBox
    ) {
        this.teiFeatureCollections = teiFeatureCollections
        this.eventsFeatureCollection = eventsFeatureCollection.featureCollectionMap
        this.teiFeatureCollections?.putAll(eventsFeatureCollection.featureCollectionMap)
        this.fieldFeatureCollections = fieldFeatures
        this.boundingBox = boundingBox

        teiImages.forEach { entry ->
            style?.removeStyleImage(entry.key)
        }

        addDynamicIcons()
        teiFeatureCollections[TEIS_SOURCE_ID]?.let {
            setTeiImages(it)
        }
        addDynamicLayers()
    }

    override fun loadDataForStyle() {
        style?.apply {
            style?.addImage(
                MapLayerManager.TEI_ICON_ID,
                getTintedDrawable(TEIS_SOURCE_ID).toBitmap()
            )
            mapStyle?.teiSymbolIcon?.let {
                addImage(
                    RelationshipMapManager.RELATIONSHIP_ICON,
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle!!.teiColor
                    )
                )
            }
            mapStyle?.enrollmentSymbolIcon?.let {
                addImage(
                    MapLayerManager.ENROLLMENT_ICON_ID,
                    getTintedDrawable(ENROLLMENT_SOURCE_ID).toBitmap()
                )
            }
            mapStyle?.stagesStyle?.keys?.forEach { key ->
                addImage(
                    "${MapLayerManager.STAGE_ICON_ID}_$key",
                    getTintedDrawable(key).toBitmap()
                )
            }
        }
        style?.addImage(
            RelationshipMapManager.RELATIONSHIP_ARROW,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.ic_arrowhead
            )!!.toBitmap(),
            true
        )
        style?.addImage(
            RelationshipMapManager.RELATIONSHIP_ICON,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.map_marker
            )!!.toBitmap(),
            true
        )
        style?.addImage(
            RelationshipMapManager.RELATIONSHIP_ARROW_BIDIRECTIONAL,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.ic_arrowhead_bidirectional
            )!!.toBitmap(),
            true
        )

        map?.addOnStyleImageMissingListener { event ->
            teiFeatureCollections?.get(TEIS_SOURCE_ID)?.features()
                ?.firstOrNull { event.id == it.getStringProperty(TEI_UID) }
                ?.let {
                    teiImages[event.id]?.let { it1 -> style?.addImage(event.id, it1) }
                } ?: mapStyle?.teiSymbolIcon?.let {
                style?.addImage(
                    event.id,
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle!!.teiColor
                    )
                )
            }
        }
        setLayer()
        addDynamicIcons()
        addDynamicLayers()
    }

    override fun setLayer() {
        mapLayerManager
            .withMapStyle(mapStyle)

        if (teiFeatureType != null && teiFeatureType != FeatureType.NONE) {
            mapLayerManager.addStartLayer(LayerType.TEI_LAYER, teiFeatureType, TEIS_SOURCE_ID)
        }
        if (enrollmentFeatureType != null && enrollmentFeatureType != FeatureType.NONE) {
            mapLayerManager
                .addLayer(LayerType.ENROLLMENT_LAYER, enrollmentFeatureType, ENROLLMENT_SOURCE_ID)
        }

        mapLayerManager.addLayer(LayerType.HEATMAP_LAYER)
    }

    override fun setSource() {
        teiFeatureCollections?.keys?.forEach {
            style?.updateSource(it, teiFeatureCollections!![it]!!)
        }
        fieldFeatureCollections.forEach {
            style?.updateSource(it.key, it.value)
        }
        addDynamicLayers()
        boundingBox?.let { initCameraPosition(it) }
    }

    private fun setTeiImages(featureCollection: FeatureCollection) {
        val featuresWithImages = featureCollection.features()
            ?.filter { it.getStringProperty(TEI_IMAGE)?.isNotEmpty() ?: false }

        featuresWithImages?.run {
            when {
                isNotEmpty() -> getImagesAndSetSource(this)
                else -> setSource()
            }
        }
    }

    private fun getImagesAndSetSource(featuresWithImages: List<Feature>) {
        featuresWithImages.forEachIndexed { index, feature ->
            Glide.with(mapView.context)
                .asBitmap()
                .load(feature.getStringProperty(TEI_IMAGE))
                .transform(CircleCrop())
                .into(object : CustomTarget<Bitmap>(30.dp, 30.dp) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        teiImages[feature.getStringProperty(TEI_UID)] = TeiMarkers.getMarker(
                            mapView.context,
                            resource
                        )
                        if (index == featuresWithImages.size - 1) {
                            setSource()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun addDynamicLayers() {
        mapLayerManager
            .updateLayers(
                LayerType.RELATIONSHIP_LAYER,
                teiFeatureCollections?.keys?.filter {
                    it != TEIS_SOURCE_ID && it != ENROLLMENT_SOURCE_ID &&
                        !eventsFeatureCollection?.containsKey(it)!!
                }?.toList() ?: emptyList()
            ).updateLayers(
                LayerType.TEI_EVENT_LAYER,
                eventsFeatureCollection?.keys?.toList() ?: emptyList()
            ).updateLayers(
                LayerType.FIELD_COORDINATE_LAYER,
                fieldFeatureCollections.keys.toList() ?: emptyList()
            )
    }

    private fun addDynamicIcons() {
        fieldFeatureCollections.entries.forEach {
            style?.addImage(
                "${EventMapManager.DE_ICON_ID}_${it.key}",
                getTintedDrawable(it.key).toBitmap()
            )
        }
        teiFeatureCollections?.entries?.filter {
            it.key != TEIS_SOURCE_ID && it.key != ENROLLMENT_SOURCE_ID &&
                !eventsFeatureCollection?.containsKey(it.key)!!
        }?.forEach {
            style?.addImage(
                "${RelationshipMapManager.RELATIONSHIP_ICON}_${it.key}",
                getTintedDrawable(it.key).toBitmap()
            )
        }
    }

    private fun getTintedDrawable(sourceId: String): Drawable {
        val (drawable, color) = mapLayerManager.getNextAvailableDrawable(sourceId) ?: Pair(
            R.drawable.map_marker,
            Color.parseColor("#E71409")
        )

        val initialDrawable = AppCompatResources.getDrawable(
            mapView.context,
            drawable
        )?.mutate()
        val wrappedDrawable = DrawableCompat.wrap(initialDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, color)

        return wrappedDrawable
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return teiFeatureCollections?.get(source)?.features()?.firstOrNull {
            it.getStringProperty(propertyName) == propertyValue
        } ?: fieldFeatureCollections[source]?.features()?.firstOrNull {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeature(propertyValue: String): Feature? {
        val mainProperties = arrayListOf(
            TEI_UID,
            RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT
        )
        var featureToReturn: Feature? = null
        mainLoop@ for (
            source in teiFeatureCollections?.filterKeys {
                it != ENROLLMENT_SOURCE_ID
            }?.keys!!
        ) {
            sourceLoop@ for (propertyLabel in mainProperties) {
                val feature = findFeature(source, propertyLabel, propertyValue)
                if (feature != null) {
                    featureToReturn = feature
                    break@sourceLoop
                }
            }
            if (featureToReturn != null) {
                break@mainLoop
            }
        }

        mainLoop@ for (source in fieldFeatureCollections.keys) {
            sourceLoop@ for (propertyLabel in mainProperties) {
                val feature = findFeature(source, propertyLabel, propertyValue)
                if (feature != null) {
                    featureToReturn = feature
                    break@sourceLoop
                }
            }
            if (featureToReturn != null) {
                break@mainLoop
            }
        }

        return featureToReturn
    }

    override fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String
    ): List<Feature> {
        return mutableListOf<Feature>().apply {
            teiFeatureCollections?.filterKeys { it != ENROLLMENT_SOURCE_ID }
                ?.map { (key, collection) ->
                    collection.features()?.filter {
                        mapLayerManager.getLayer(key)?.visible == true &&
                            it.getStringProperty(propertyName) == propertyValue
                    }?.map {
                        mapLayerManager.getLayer(key)?.setSelectedItem(it)
                        it
                    }?.let {
                        mapLayerManager.getLayer(key)?.setSelectedItem(it)
                        addAll(it)
                    }
                }

            teiFeatureCollections?.filterKeys {
                it == ENROLLMENT_SOURCE_ID && mapLayerManager.getLayer(
                    ENROLLMENT_SOURCE_ID
                )?.visible == true
            }
                ?.map { (key, collection) ->
                    collection.features()?.filter {
                        it.getStringProperty(propertyName) == propertyValue
                    }?.map {
                        mapLayerManager.getLayer(ENROLLMENT_SOURCE_ID)?.setSelectedItem(it)
                        it
                    }?.let { addAll(it) }
                }

            fieldFeatureCollections.values.map { collection ->
                collection.features()?.filter {
                    mapLayerManager.getLayer(
                        it.getStringProperty(MapCoordinateFieldToFeatureCollection.FIELD_NAME)
                    )?.visible == true &&
                        it.getStringProperty(propertyName) == propertyValue
                }?.map {
                    mapLayerManager.getLayer(
                        it.getStringProperty(MapCoordinateFieldToFeatureCollection.FIELD_NAME)
                    )?.setSelectedItem(it)
                    it
                }?.let { addAll(it) }
            }
        }
    }

    override fun findFeatures(propertyValue: String): List<Feature>? {
        val mainProperties = arrayListOf(
            TEI_UID,
            RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT
        )

        return mainProperties.map { property ->
            findFeatures("", property, propertyValue)
        }.flatten().distinct()
    }

    override fun getLayerName(source: String): String {
        return if (fieldFeatureCollections.containsKey(source)) {
            fieldFeatureCollections[source]?.features()?.get(0)?.let {
                if (it.hasProperty(MapCoordinateFieldToFeatureCollection.STAGE)) {
                    "${
                    it.getStringProperty(
                        MapCoordinateFieldToFeatureCollection.STAGE
                    )
                    } - ${
                    it.getStringProperty(
                        MapCoordinateFieldToFeatureCollection.FIELD_NAME
                    )
                    }"
                } else {
                    it.getStringProperty(MapCoordinateFieldToFeatureCollection.FIELD_NAME)
                }
            } ?: super.getLayerName(source)
        } else {
            super.getLayerName(source)
        }
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
        val sourcesAndLayers = mapLayerManager.sourcesAndLayersForSearch()
        sourcesAndLayers.filter { it.value.isNotEmpty() }.forEach { (source, layer) ->
            map?.queryRenderedFeatures(
                RenderedQueryGeometry(screenCoordinate),
                RenderedQueryOptions(listOf(layer.first()), null)
            ) { expected ->
                if (expected.isValue && expected.value?.size!! > 0) {
                    expected.value?.get(0)?.feature?.let { feature ->
                        if (selectedFeature == null) {
                            mapLayerManager.selectFeature(null)
                        }
                        if (selectedFeature == null || source.contains(TEIS_SOURCE_ID)) {
                            selectedFeature = when {
                                layer.any { it.contains("RELATIONSHIP") } -> findFeature(
                                    source,
                                    RELATIONSHIP_UID,
                                    feature.getStringProperty(RELATIONSHIP_UID)
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
