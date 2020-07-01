package org.dhis2.uicomponents.map.managers

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.HashMap
import org.dhis2.uicomponents.map.TeiMarkers
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.geometry.mapper.EventsByProgramStage
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
                    MapLayerManager.TEI_ICON_ID,
                    TeiMarkers.getMarker(
                        mapView.context,
                        it,
                        mapStyle.teiColor
                    )
                )
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
        }
        setSource()
        setLayer()
        teiFeatureCollections[TEIS_SOURCE_ID]?.let { setSymbolManager(it) }
    }

    override fun setSource() {
        teiFeatureCollections.keys.forEach {
            style?.getSourceAs<GeoJsonSource>(it)?.setGeoJson(teiFeatureCollections[it])
                ?: style?.addSource(GeoJsonSource(it, teiFeatureCollections[it]))
        }
        initCameraPosition(boundingBox)
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
}
