package org.dhis2.utils.maps

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.common.FeatureType

object GeometryUtils {

    fun getSourceFromTeis(teiList: List<SearchTeiModel>): GeoJsonSource {

        val features: ArrayList<Feature> = ArrayList()

        teiList.forEach {
            if(it.tei.geometry()!=null) {
                val geometry = it.tei.geometry()!!

                if (geometry.type() == FeatureType.POINT) {

                } else if (geometry.type() == FeatureType.POLYGON) {
                    val feature = Feature.fromJson(geometry.toString())
                    feature.addStringProperty("uid", it.tei.uid())
                    features.add(feature)
                }
            }
        }

        val featureCollection = FeatureCollection.fromFeatures(features)

        return GeoJsonSource("teis", featureCollection)
    }

}
