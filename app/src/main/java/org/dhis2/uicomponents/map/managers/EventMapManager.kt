package org.dhis2.uicomponents.map.managers

import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.uicomponents.map.layer.LayerType
import org.hisp.dhis.android.core.common.FeatureType

class EventMapManager : MapManager() {

    private lateinit var boundingBox: BoundingBox
    private lateinit var featureCollection: FeatureCollection

    companion object {
        const val ICON_ID = "ICON_ID"
        const val EVENTS = "events"
    }

    fun update(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox,
        featureType: FeatureType?
    ) {
        this.featureType = featureType ?: FeatureType.POINT
        this.featureCollection = featureCollection
        this.boundingBox = boundingBox
        if (isMapReady()) {
            (style?.getSource(EVENTS) as GeoJsonSource?)?.setGeoJson(featureCollection)
                ?: loadDataForStyle()
        }
    }

    override fun loadDataForStyle() {
        style?.addImage(
            ICON_ID,
            AppCompatResources.getDrawable(
                mapView.context,
                R.drawable.map_marker
            )!!
        )
        setSource()
        setLayer()
        setSymbolManager(featureCollection)
        initCameraPosition(boundingBox)
    }

    override fun setSource() {
        style?.addSource(GeoJsonSource(EVENTS, featureCollection))
    }

    override fun setLayer() {
        mapLayerManager.initMap(map)
            .withFeatureType(featureType)
            .addStartLayer(LayerType.EVENT_LAYER)
    }
}
