package org.dhis2.maps.managers

import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.dhis2.maps.R
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection.Companion.RELATIONSHIP_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.layer.LayerType

class RelationshipMapManager(mapView: MapView) : MapManager(mapView) {

    companion object {
        const val RELATIONSHIP_ICON = "RELATIONSHIP_ICON"
        const val RELATIONSHIP_ARROW = "RELATIONSHIP_ARROW"
        const val RELATIONSHIP_ARROW_BIDIRECTIONAL = "RELATIONSHIP_ARROW_BIDIRECTIONAL"
    }

    private lateinit var boundingBox: BoundingBox
    private lateinit var featureCollections: Map<String, FeatureCollection>

    fun update(featureCollections: Map<String, FeatureCollection>, boundingBox: BoundingBox) {
        this.featureCollections = featureCollections
        this.boundingBox = boundingBox
        if (isMapReady()) {
            when {
                mapLayerManager.mapLayers.isNotEmpty() -> updateStyleSources()
                else -> loadDataForStyle()
            }
        }
    }

    override fun loadDataForStyle() {
        style?.addImage(
            RELATIONSHIP_ARROW,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead,
                ),
            )!!,
            true,
        )
        style?.addImage(
            RELATIONSHIP_ICON,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.map_marker,
                ),
            )!!,
            true,
        )
        style?.addImage(
            RELATIONSHIP_ARROW_BIDIRECTIONAL,
            BitmapUtils.getBitmapFromDrawable(
                AppCompatResources.getDrawable(
                    mapView.context,
                    R.drawable.ic_arrowhead_bidirectional,
                ),
            )!!,
            true,
        )
        addDynamicIcons()
        setSource()
        setLayer()
    }

    private fun addDynamicIcons() {
        featureCollections.entries.forEach {
            style?.addImage(
                "${RELATIONSHIP_ICON}_${it.key}",
                getTintedDrawable(it.key),
            )
        }
    }

    private fun getTintedDrawable(sourceId: String): Drawable {
        val (drawable, color) = mapLayerManager.getNextAvailableDrawable(sourceId) ?: Pair(
            R.drawable.map_marker,
            Color.parseColor("#E71409"),
        )

        val initialDrawable = AppCompatResources.getDrawable(
            mapView.context,
            drawable,
        )?.mutate()
        val wrappedDrawable = DrawableCompat.wrap(initialDrawable!!)
        DrawableCompat.setTint(wrappedDrawable, color)

        return wrappedDrawable
    }

    private fun updateStyleSources() {
        setSource()
        mapLayerManager.updateLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList())
    }

    override fun setSource() {
        featureCollections.keys.forEach {
            style?.getSourceAs<GeoJsonSource>(it)?.setGeoJson(featureCollections[it])
                ?: style?.addSource(GeoJsonSource(it, featureCollections[it]))
        }
        initCameraPosition(boundingBox)
    }

    override fun setLayer() {
        mapLayerManager
            .addLayers(LayerType.RELATIONSHIP_LAYER, featureCollections.keys.toList(), true)
    }

    override fun findFeature(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): Feature? {
        return featureCollections[source]?.features()?.firstOrNull {
            it.getStringProperty(propertyName) == propertyValue
        }
    }

    override fun findFeature(propertyValue: String): Feature? {
        val mainProperties = arrayListOf(
            MapTeisToFeatureCollection.TEI_UID,
            MapTeisToFeatureCollection.ENROLLMENT_UID,
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT,
        )
        var featureToReturn: Feature? = null
        for (source in featureCollections.keys) {
            for (propertyLabel in mainProperties) {
                val feature = findFeature(source, propertyLabel, propertyValue)
                if (feature != null) {
                    featureToReturn = feature
                    mapLayerManager.getLayer(source, true)?.setSelectedItem(featureToReturn)
                    break
                }
                if (featureToReturn != null) {
                    break
                }
            }
        }
        return featureToReturn
    }

    override fun findFeatures(
        source: String,
        propertyName: String,
        propertyValue: String,
    ): List<Feature> {
        return mutableListOf<Feature>().apply {
            featureCollections.map { (key, collection) ->
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
        }
    }

    override fun findFeatures(propertyValue: String): List<Feature>? {
        val mainProperties = arrayListOf(
            MapTeisToFeatureCollection.TEI_UID,
            MapTeisToFeatureCollection.ENROLLMENT_UID,
            MapRelationshipsToFeatureCollection.RELATIONSHIP_UID,
            MapEventToFeatureCollection.EVENT,
        )
        return mainProperties.map { property ->
            findFeatures("", property, propertyValue)
        }.flatten().distinct()
    }

    override fun markFeatureAsSelected(point: LatLng, layer: String?): Feature? {
        val pointf: PointF = map?.projection?.toScreenLocation(point)!!
        val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)

        return selectedFeature(rectF)
    }

    private fun selectedFeature(rectF: RectF): Feature? {
        var selectedFeature: Feature? = null
        val sourcesAndLayers = mapLayerManager.sourcesAndLayersForSearch()
        sourcesAndLayers.filter { it.value.isNotEmpty() }.forEach { (_, layers) ->
            val features = map?.queryRenderedFeatures(rectF, *layers) ?: emptyList()
            if (features.isNotEmpty()) {
                mapLayerManager.selectFeature(null)
                selectedFeature = features.first()
                map
            }
        }
        return selectedFeature
    }
}
