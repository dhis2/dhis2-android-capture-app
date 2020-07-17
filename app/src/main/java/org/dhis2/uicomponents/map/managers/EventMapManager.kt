package org.dhis2.uicomponents.map.managers

import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.layer.LayerType
import org.dhis2.usescases.events.EXTRA_EVENT_UID
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
            .addLayer(LayerType.SATELLITE_LAYER)
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String
    ): Feature? {
        return featureCollection.features()?.firstOrNull() {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeature(propertyValue: String): Feature? {
        val mainProperties = arrayListOf(
            MapTeisToFeatureCollection.TEI_UID,
            MapTeisToFeatureCollection.ENROLLMENT_UID,
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID, EXTRA_EVENT_UID
        )
        var featureToReturn: Feature? = null
        for (propertyLabel in mainProperties) {
            val feature = findFeature(EVENTS, propertyLabel, propertyValue)
            if (feature != null) {
                featureToReturn = feature
                mapLayerManager.getLayer(EVENTS, true)?.setSelectedItem(featureToReturn)
                break
            }
            if (featureToReturn != null) {
                break
            }
        }
        return featureToReturn
    }
}
