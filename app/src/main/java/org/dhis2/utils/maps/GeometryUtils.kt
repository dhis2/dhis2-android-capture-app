package org.dhis2.utils.maps

import androidx.annotation.VisibleForTesting
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

object GeometryUtils {

    private var northBound: Double = 0.0
    private var southBound: Double = 0.0
    private var eastBound: Double = 0.0
    private var westBound: Double = 0.0
    private var boundsInt = false

    fun getSourceFromTeis(teiList: List<SearchTeiModel>): Pair<FeatureCollection, BoundingBox> {
        boundsInt = false
        northBound = 0.0
        southBound = 0.0
        eastBound = 0.0
        westBound = 0.0

        val features: ArrayList<Feature> = ArrayList()

        teiList.forEach {
            if (it.tei.geometry() != null) {
                val geometry = it.tei.geometry()!!

                if (geometry.type() == FeatureType.POINT) {

                } else if (geometry.type() == FeatureType.POLYGON) {
                    val polygon = getPolygonFeature(geometry)
                    polygon.addStringProperty("teiUid", it.tei.uid())
                    features.add(polygon)
                }
            }
        }


        return Pair<FeatureCollection, BoundingBox>(FeatureCollection.fromFeatures(features),
                BoundingBox.fromLngLats(westBound, southBound, eastBound, northBound))

    }

    @VisibleForTesting
    private fun getPolygonFeature(geometry: Geometry): Feature {


        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val pointList = ArrayList<Point>()
        sdkPolygon.forEach {
            it.forEach { coordinates ->
                val lat = coordinates[0]
                val lon = coordinates[1]

                if (!boundsInt) {
                    northBound = lat
                    southBound = lat
                    eastBound = lon
                    westBound = lon
                    boundsInt = true
                } else {

                    if (northBound < lat)
                        northBound = lat
                    if (southBound > lat)
                        southBound = lat
                    if (eastBound < lon)
                        eastBound = lon
                    if (westBound > lon)
                        westBound = lon
                }

                pointList.add(Point.fromLngLat(lon, lat))
            }
        }
        val polygonArray = ArrayList<ArrayList<Point>>()
        polygonArray.add(pointList)
        val polygon = Polygon.fromLngLats(polygonArray as List<MutableList<Point>>)
        return Feature.fromGeometry(polygon)
    }

    fun getTestingGeoJsonSource(): GeoJsonSource {
        return GeoJsonSource("teis", "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [100.0, -1.0], [101.0, -1.0], [101.0, -2.0],\n" +
                "            [100.0, -2.0], [100.0, -1.0]\n" +
                "          ]\n" +
                "          ]\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"prop0\": \"value0\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [102.0, 1.0], [103.0, 1.0], [103.0, 2.0],\n" +
                "            [102.0, 2.0], [102.0, 1.0]\n" +
                "          ]\n" +
                "        ]\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"prop0\": \"value0\",\n" +
                "        \"prop1\": 0.0\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [100.0, 0.5], [100.5, 0.0], [101.0, 0.5],\n" +
                "            [100.5, 1.0], [100.0, 0.5]\n" +
                "          ]\n" +
                "        ]\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"prop0\": \"value0\",\n" +
                "        \"prop1\": { \"this\": \"that\" }\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}")
    }

}
