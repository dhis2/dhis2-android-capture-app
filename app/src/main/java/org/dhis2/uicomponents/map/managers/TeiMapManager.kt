package org.dhis2.uicomponents.map.managers

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.CustomTarget
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.dhis2.Bindings.dp
import java.util.ArrayList
import java.util.HashMap
import org.dhis2.R
import org.dhis2.uicomponents.map.TeiMarkers
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.Companion.RELATIONSHIP_UID
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.ENROLLMENT_UID
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_IMAGE
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_UID
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.dhis2.uicomponents.map.model.MapStyle
import org.hisp.dhis.android.core.common.FeatureType

class TeiMapManager(
    private val mapStyle: MapStyle
) : MapManager() {

    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var boundingBox: BoundingBox
    private lateinit var teiFeatureCollections: HashMap<String, FeatureCollection>
    private lateinit var eventsFeatureCollection: Map<String, FeatureCollection>
    private var teiImages: HashMap<String, Bitmap> = hashMapOf()

    companion object {
        const val TEIS_SOURCE_ID = "TEIS_SOURCE_ID"
        const val ENROLLMENT_SOURCE_ID = "ENROLLMENT_SOURCE_ID"
    }

    fun update(
        teiFeatureCollections: HashMap<String, FeatureCollection>,
        eventsFeatureCollection: EventsByProgramStage,
        boundingBox: BoundingBox,
        featureType: FeatureType,
        carouselAdapter: CarouselAdapter
    ) {
        this.featureType = featureType
        this.teiFeatureCollections = teiFeatureCollections
        this.eventsFeatureCollection = eventsFeatureCollection.featureCollectionMap
        this.teiFeatureCollections.putAll(this.eventsFeatureCollection)
        this.boundingBox = boundingBox
        this.carouselAdapter = carouselAdapter
        if (isMapReady()) {
            when {
                mapLayerManager.mapLayers.isNotEmpty() -> updateStyleSources()
                else -> loadDataForStyle()
            }
        }
    }

    override fun loadDataForStyle() {
        style?.apply {
            mapStyle.teiSymbolIcon?.let {
                addImage(
                    RelationshipMapManager.RELATIONSHIP_ICON,
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle.teiColor
                    )
                )
            }
            mapStyle.enrollmentSymbolIcon?.let {
                addImage(
                    MapLayerManager.ENROLLMENT_ICON_ID,
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle.enrollmentColor
                    )
                )
            }
            mapStyle.stagesStyle.keys.forEach { key ->
                addImage(
                    "${MapLayerManager.STAGE_ICON_ID}_$key",
                    TeiMarkers.getMarker(
                        mapView.context,
                        mapStyle.stagesStyle[key]!!.stageIcon,
                        mapStyle.stagesStyle[key]!!.stageColor
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
            teiFeatureCollections[TEIS_SOURCE_ID]?.features()
                ?.firstOrNull { id == it.getStringProperty(TEI_UID) }
                ?.let {
                    teiImages[id]?.let { it1 -> style?.addImageAsync(id, it1) }
                } ?: mapStyle.teiSymbolIcon?.let {
                style?.addImageAsync(
                    id,
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle.teiColor
                    )
                )
            }
        }
        teiFeatureCollections[TEIS_SOURCE_ID]?.let {
            setSymbolManager(it)
            setTeiImages(it)
        }
        setSource()
        setLayer()
    }

    override fun setSource() {
        teiFeatureCollections.keys.forEach {
            style?.getSourceAs<GeoJsonSource>(it)?.setGeoJson(teiFeatureCollections[it])
                ?: style?.addSource(GeoJsonSource(it, teiFeatureCollections[it]))
        }
        initCameraPosition(boundingBox)
    }

    private fun setTeiImages(featureCollection: FeatureCollection) {
        featureCollection.features()
            ?.filter { it.getStringProperty(TEI_IMAGE)?.isNotEmpty() ?: false }
            ?.forEach {
                Glide.with(mapView.context)
                    .asBitmap()
                    .load(it.getStringProperty(TEI_IMAGE))
                    .transform(CircleCrop())
                    .into(object : CustomTarget<Bitmap>(23.dp, 23.dp) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            teiImages[it.getStringProperty(TEI_UID)] = TeiMarkers.getMarker(
                                mapView.context,
                                resource
                            )
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
    }

    private fun updateStyleSources() {
        setSource()
        mapLayerManager.updateLayers(
            LayerType.RELATIONSHIP_LAYER,
            teiFeatureCollections.keys.toList()
        )
    }

    override fun setLayer() {
        mapLayerManager.initMap(map)
            .withFeatureType(featureType)
            .withMapStyle(mapStyle)
            .withCarousel(carouselAdapter)
            .addStartLayer(LayerType.TEI_LAYER, TEIS_SOURCE_ID)
            .addLayer(LayerType.ENROLLMENT_LAYER, ENROLLMENT_SOURCE_ID)
            .addLayer(LayerType.HEATMAP_LAYER)
            .addLayer(LayerType.SATELLITE_LAYER)
            .addLayers(
                LayerType.RELATIONSHIP_LAYER,
                teiFeatureCollections.keys.filter {
                    it != TEIS_SOURCE_ID && it != ENROLLMENT_SOURCE_ID
                },
                false
            )
            .addLayers(
                LayerType.TEI_EVENT_LAYER,
                eventsFeatureCollection.keys.toList(),
                false
            )
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return teiFeatureCollections[source]?.features()?.firstOrNull {
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
        mainLoop@ for (source in teiFeatureCollections.keys) {
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

    fun getSourcesAndLayersForSearch(): Pair<List<String>, List<Array<String>>> {
        val layers: MutableList<Array<String>> =
            ArrayList()
        val sources: MutableList<String> =
            ArrayList()
        layers.add(
            arrayOf(
                if (featureType == FeatureType.POINT) {
                    "TEI_POINT_LAYER_ID"
                } else {
                    "TEI_POLYGON_LAYER_ID"
                }
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
        for (sourceId in teiFeatureCollections.keys) {
            layers.add(arrayOf("RELATIONSHIP_LINE_LAYER_ID_$sourceId"))
            sources.add(sourceId)
        }
        for (eventSource in eventsFeatureCollection.keys) {
            layers.add(
                arrayOf(
                    "POINT_LAYER_$eventSource",
                    "POLYGON_LAYER$eventSource"
                )
            )
            sources.add(eventSource)
        }

        return Pair(sources, layers)
    }
}
