package org.dhis2.uicomponents.map.managers

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import java.util.ArrayList
import java.util.HashMap
import org.dhis2.Bindings.dp
import org.dhis2.R
import org.dhis2.uicomponents.map.TeiMarkers
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.Companion.RELATIONSHIP_UID
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.ENROLLMENT_UID
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_IMAGE
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_UID
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.dhis2.uicomponents.map.model.MapStyle
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
        addDynamicIcons()
        teiFeatureCollections[TEIS_SOURCE_ID]?.let {
            setTeiImages(it)
        }
        addDynamicLayers()
    }

    override fun loadDataForStyle() {
        style?.apply {
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
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle!!.enrollmentColor
                    )
                )
            }
            mapStyle?.stagesStyle?.keys?.forEach { key ->
                addImage(
                    "${MapLayerManager.STAGE_ICON_ID}_$key",
                    TeiMarkers.getMarker(
                        mapView.context,
                        mapStyle!!.stagesStyle[key]!!.stageIcon,
                        mapStyle!!.stagesStyle[key]!!.stageColor
                    )
                )
            }
        }
        style?.addImage(
            RelationshipMapManager.RELATIONSHIP_ARROW,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead
                )
            )!!,
            true
        )
        style?.addImage(
            RelationshipMapManager.RELATIONSHIP_ICON,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.map_marker
                )
            )!!,
            true
        )
        style?.addImage(
            RelationshipMapManager.RELATIONSHIP_ARROW_BIDIRECTIONAL,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead_bidirectional
                )
            )!!,
            true
        )

        mapView.addOnStyleImageMissingListener { id ->
            teiFeatureCollections?.get(TEIS_SOURCE_ID)?.features()
                ?.firstOrNull { id == it.getStringProperty(TEI_UID) }
                ?.let {
                    teiImages[id]?.let { it1 -> style?.addImage(id, it1) }
                } ?: mapStyle?.teiSymbolIcon?.let {
                style?.addImage(
                    id,
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
            .withCarousel(carouselAdapter)
            .addStartLayer(LayerType.TEI_LAYER, teiFeatureType, TEIS_SOURCE_ID)
            .addLayer(LayerType.ENROLLMENT_LAYER, enrollmentFeatureType, ENROLLMENT_SOURCE_ID)
            .addLayer(LayerType.HEATMAP_LAYER)
            .addLayer(LayerType.SATELLITE_LAYER)
    }

    override fun setSource() {
        teiFeatureCollections?.keys?.forEach {
            style?.getSourceAs<GeoJsonSource>(it)?.setGeoJson(teiFeatureCollections!![it])
                ?: style?.addSource(GeoJsonSource(it, teiFeatureCollections!![it]))
        }
        fieldFeatureCollections.forEach {
            (style?.getSource(it.key) as GeoJsonSource?)?.setGeoJson(it.value)
                ?: style?.addSource(GeoJsonSource(it.key, it.value))
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
                .into(object : CustomTarget<Bitmap>(23.dp, 23.dp) {
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

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return teiFeatureCollections?.get(source)?.features()?.firstOrNull {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeature(propertyValue: String): Feature? {
        val mainProperties = arrayListOf(
            TEI_UID,
            ENROLLMENT_UID,
            RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT
        )
        var featureToReturn: Feature? = null
        mainLoop@ for (source in teiFeatureCollections!!.keys) {
            sourceLoop@ for (propertyLabel in mainProperties) {
                val feature = findFeature(source, propertyLabel, propertyValue)
                if (feature != null) {
                    featureToReturn = feature
                    mapLayerManager.getLayer(source, true)?.setSelectedItem(featureToReturn)
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
    ): List<Feature>? {
        return mutableListOf<Feature>().apply {
            teiFeatureCollections?.values?.map { collection ->
                collection.features()?.filter {
                    it.getStringProperty(propertyName) == propertyValue
                }?.map {
                    mapLayerManager.getLayer(TEIS_SOURCE_ID)
                        ?.setSelectedItem(it)
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
        return findFeatures("", TEI_UID, propertyValue)
    }

    fun getSourcesAndLayersForSearch(): Pair<List<String>, List<Array<String>>> {
        val layers: MutableList<Array<String>> =
            ArrayList()
        val sources: MutableList<String> =
            ArrayList()
        layers.add(
            arrayOf(
                "TEI_POINT_LAYER_ID",
                "TEI_POLYGON_LAYER_ID"
            )
        )
        sources.add(TEIS_SOURCE_ID)
        layers.add(
            arrayOf(
                "ENROLLMENT_POINT_LAYER_ID",
                "ENROLLMENT_POLYGON_LAYER_ID"
            )
        )
        sources.add(ENROLLMENT_SOURCE_ID)
        teiFeatureCollections?.keys?.forEach { sourceId ->
            layers.add(arrayOf("RELATIONSHIP_LINE_LAYER_ID_$sourceId"))
            sources.add(sourceId)
        }
        eventsFeatureCollection?.keys?.forEach { eventSource ->
            layers.add(
                arrayOf(
                    "POINT_LAYER_$eventSource",
                    "POLYGON_LAYER$eventSource"
                )
            )
            sources.add(eventSource)
        }
        fieldFeatureCollections.keys.forEach { deSource ->
            layers.add(
                arrayOf(
                    "DE_POINT_LAYER_ID_$deSource"
                )
            )
            sources.add(deSource)
        }

        return Pair(sources, layers)
    }

    override fun getLayerName(source: String): String {
        return if (fieldFeatureCollections.containsKey(source)) {
            fieldFeatureCollections[source]?.features()?.get(0)?.let {
                if (it.hasProperty(MapCoordinateFieldToFeatureCollection.STAGE)) {
                    "${it.getStringProperty(
                        MapCoordinateFieldToFeatureCollection.STAGE
                    )} - ${it.getStringProperty(
                        MapCoordinateFieldToFeatureCollection.FIELD_NAME
                    )}"
                } else {
                    it.getStringProperty(MapCoordinateFieldToFeatureCollection.FIELD_NAME)
                }
            } ?: super.getLayerName(source)
        } else {
            super.getLayerName(source)
        }
    }

    override fun markFeatureAsSelected(point: LatLng, layer: String?): Feature? {
        val pointf: PointF = map.projection.toScreenLocation(point)
        val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)

        val (sources, layers) = getSourcesAndLayersForSearch()

        layer?.let {
            return selectFeatureForLayer(rectF, TEIS_SOURCE_ID, it)
        }

        return selectedFeature(rectF, sources, layers, 0)
    }

    private fun selectedFeature(
        rectF: RectF,
        sources: List<String>,
        layers: List<Array<String>>,
        count: Int
    ): Feature? {
        val source = sources[count]
        val layersToSearch = layers[count]
        val features: List<Feature> = map.queryRenderedFeatures(rectF, *layersToSearch)
        var selectedFeature: Feature? = null
        return when {
            features.isNotEmpty() -> {
                mapLayerManager.selectFeature(null)
                selectedFeature = features[0]
                if (source.contains("RELATIONSHIP")) {
                    selectedFeature = findFeature(
                        source,
                        RELATIONSHIP_UID,
                        selectedFeature!!.getStringProperty(RELATIONSHIP_UID)
                    )
                }
                mapLayerManager.getLayer(source, true)?.setSelectedItem(selectedFeature)
                selectedFeature
            }
            count < sources.size - 1 -> { selectedFeature(rectF, sources, layers, count + 1) }
            else -> selectedFeature
        }
    }

    private fun selectFeatureForLayer(rectF: RectF, source: String, layer: String): Feature? {
        val features: List<Feature> = map.queryRenderedFeatures(rectF, layer)
        var feature: Feature? = null
        if (features.isNotEmpty()) {
            if (source.contains(TEIS_SOURCE_ID)) {
                feature = features[0]
            }
        }
        return feature
    }
}
