package org.dhis2.uicomponents.map.layer

import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.uicomponents.map.MapStyle
import org.dhis2.uicomponents.map.layer.types.EnrollmentMapLayer
import org.dhis2.uicomponents.map.layer.types.HeatmapMapLayer
import org.dhis2.uicomponents.map.layer.types.SatelliteMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiMapLayer
import org.hisp.dhis.android.core.common.FeatureType

class MapLayerManager {

    private lateinit var mapLayers: Map<LayerType, MapLayer>
    var styleChangeCallback: (() -> Unit)? = null

    companion object {
        const val TEI_ICON_ID = "TEI_ICON_ID"
        const val ENROLLMENT_ICON_ID = "ENROLLMENT_ICON_ID"
    }

    fun init(
        mapboxMap: MapboxMap,
        featureType: FeatureType,
        mapStyle: MapStyle
    ) {
        val style = mapboxMap.style!!
        mapLayers = mapOf(
            LayerType.TEI_LAYER to TeiMapLayer(
                style,
                featureType,
                mapStyle.teiColor,
                mapStyle.programDarkColor
            ),
            LayerType.ENROLLMENT_LAYER to EnrollmentMapLayer(
                style,
                featureType,
                mapStyle.enrollmentColor,
                mapStyle.programDarkColor
            ),
            LayerType.HEATMAP_LAYER to HeatmapMapLayer(
                style,
                featureType
            ),
            LayerType.SATELLITE_LAYER to SatelliteMapLayer(
                mapboxMap,
                styleChangeCallback
            )
        )
    }

    fun handleLayer(type: LayerType, check: Boolean){
        when {
            check -> mapLayers[type]?.showLayer()
            else -> mapLayers[type]?.hideLayer()
        }
    }
}
