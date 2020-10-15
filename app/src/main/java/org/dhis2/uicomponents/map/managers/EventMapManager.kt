package org.dhis2.uicomponents.map.managers

import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.layer.LayerType
import org.hisp.dhis.android.core.common.FeatureType

class EventMapManager(mapView: MapView) : MapManager(mapView) {

    private var featureCollection: FeatureCollection? = null
    var featureType: FeatureType? = null

    companion object {
        const val ICON_ID = "ICON_ID"
        const val EVENTS = "events"
    }

    fun update(
        featureCollection: FeatureCollection,
        boundingBox: BoundingBox
    ) {
        this.featureCollection = featureCollection
        initCameraPosition(boundingBox)
        setSymbolManager(featureCollection)
        setSource()
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
    }

    override fun setSource() {
        (style?.getSource(EVENTS) as GeoJsonSource?)?.setGeoJson(featureCollection)
            ?: style?.addSource(GeoJsonSource(EVENTS, featureCollection))
    }

    override fun setLayer() {
        mapLayerManager
            .addStartLayer(LayerType.EVENT_LAYER, featureType)
            .addLayer(LayerType.SATELLITE_LAYER)
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
}
