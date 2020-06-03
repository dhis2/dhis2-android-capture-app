package org.dhis2.uicomponents.map.managers

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.HashMap
import org.dhis2.uicomponents.map.MapStyle
import org.dhis2.uicomponents.map.TeiMarkers
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.hisp.dhis.android.core.common.FeatureType

class TeiMapManager(
    private val mapStyle: MapStyle
) : MapManager() {

    private lateinit var teiFeatureCollections: HashMap<String, FeatureCollection>

    companion object {
        const val TEIS_SOURCE_ID = "TEIS_SOURCE_ID"
        const val ENROLLMENT_SOURCE_ID = "ENROLLMENT_SOURCE_ID"
        const val TEI = "TEI"
        const val ENROLLMENT = "ENROLLMENT"
    }

    fun update(
        teiFeatureCollections: HashMap<String, FeatureCollection>,
        boundingBox: BoundingBox,
        featureType: FeatureType
    ) {
        this.featureType = featureType
        this.teiFeatureCollections = teiFeatureCollections
        (style?.getSource(TEIS_SOURCE_ID) as GeoJsonSource?)
            ?.setGeoJson(teiFeatureCollections[TEI])
            .also {
                (style?.getSource(ENROLLMENT_SOURCE_ID) as GeoJsonSource?)
                    ?.setGeoJson(teiFeatureCollections[ENROLLMENT])
            } ?: run {
            loadDataForStyle()
        }
        initCameraPosition(boundingBox)
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
        teiFeatureCollections[TEI]?.let { setSymbolManager(it) }
    }

    override fun setSource() {
        style?.addSource(GeoJsonSource(TEIS_SOURCE_ID, teiFeatureCollections[TEI]))
        style?.addSource(GeoJsonSource(ENROLLMENT_SOURCE_ID, teiFeatureCollections[ENROLLMENT]))
    }

    override fun setLayer() {
        mapLayerManager.initMap(map)
            .withFeatureType(featureType)
            .withMapStyle(mapStyle)
            .addStartLayer(LayerType.TEI_LAYER, TEIS_SOURCE_ID)
            .addLayer(LayerType.ENROLLMENT_LAYER, ENROLLMENT_SOURCE_ID)
            .addLayer(LayerType.HEATMAP_LAYER)
            .addLayer(LayerType.SATELLITE_LAYER)
    }
}
