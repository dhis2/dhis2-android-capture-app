package org.dhis2.utils.maps

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.event.Event
import timber.log.Timber

object GeometryUtils {

    private var northBound: Double = 0.0
    private var southBound: Double = 0.0
    private var eastBound: Double = 0.0
    private var westBound: Double = 0.0
    private var boundsInt = false

    fun getSourceFromTeis(
        teiList: List<SearchTeiModel>
    ): Pair<HashMap<String, FeatureCollection>, BoundingBox> {
        boundsInt = false
        northBound = 0.0
        southBound = 0.0
        eastBound = 0.0
        westBound = 0.0

        val featureMap: HashMap<String, ArrayList<Feature>> = HashMap()
        featureMap["TEI"] = ArrayList()
        featureMap["ENROLLMENT"] = ArrayList()

        teiList.forEach {
            if (it.tei.geometry() != null) {
                val geometry = it.tei.geometry()!!

                if (geometry.type() == FeatureType.POINT) {
                    val point = getPointFeature(geometry)
                    if (point != null) {
                        point.addStringProperty("teiUid", it.tei.uid())
                        point.addStringProperty("teiImage", it.profilePicturePath)
                        if (it.selectedEnrollment != null){
                            point.addStringProperty("enrollmentUid", it.selectedEnrollment.uid())
                        }
                        featureMap["TEI"]!!.add(point)
                    }
                } else if (geometry.type() == FeatureType.POLYGON) {
                    val polygon = getPolygonFeature(geometry)
                    polygon.addStringProperty("teiUid", it.tei.uid())
                    polygon.addStringProperty("teiImage", it.profilePicturePath)
                    featureMap["TEI"]!!.add(polygon)
                    val polygonPoint = getPolygonPointFeature(geometry)
                    polygonPoint.addStringProperty("teiUid", it.tei.uid())
                    polygonPoint.addStringProperty("teiImage", it.profilePicturePath)
                    featureMap["TEI"]!!.add(polygonPoint)
                }
            }

            if (it.selectedEnrollment != null && it.selectedEnrollment.geometry() != null) {
                val geometry = it.selectedEnrollment.geometry()!!
                if (geometry.type() == FeatureType.POINT) {
                    val point = getPointFeature(geometry)
                    if (point != null) {
                        point.addStringProperty("enrollmentUid", it.selectedEnrollment.uid())
                        point.addStringProperty("teiUid", it.tei.uid())
                        featureMap["ENROLLMENT"]!!.add(point)
                    }
                } else if (geometry.type() == FeatureType.POLYGON) {
                    val polygon = getPolygonFeature(geometry)
                    polygon.addStringProperty("enrollmentUid", it.selectedEnrollment.uid())
                    polygon.addStringProperty("teiUid", it.tei.uid())
                    featureMap["ENROLLMENT"]!!.add(polygon)
                    val polygonPoint = getPolygonPointFeature(geometry)
                    polygonPoint.addStringProperty("teiUid", it.tei.uid())
                    featureMap["ENROLLMENT"]!!.add(polygonPoint)
                }
            }
        }

        val teiFeatureCollection = FeatureCollection.fromFeatures(featureMap["TEI"] as ArrayList)
        val enrollmentFeatureCollection =
            FeatureCollection.fromFeatures(featureMap["ENROLLMENT"] as ArrayList)

        val featureCollectionMap = HashMap<String, FeatureCollection>()
        featureCollectionMap["TEI"] = teiFeatureCollection
        featureCollectionMap["ENROLLMENT"] = enrollmentFeatureCollection

        return Pair<HashMap<String, FeatureCollection>, BoundingBox>(
            featureCollectionMap,
            BoundingBox.fromLngLats(westBound, southBound, eastBound, northBound)
        )
    }

    fun getSourceFromEvent(eventList: List<Event>): Pair<FeatureCollection, BoundingBox> {
        boundsInt = false
        northBound = 0.0
        southBound = 0.0
        eastBound = 0.0
        westBound = 0.0

        val features = eventList
            .filter { it.geometry() != null }
            .map {
                handleGeometry(it.geometry()!!, "eventUid", it.uid()!!)
            }
            .filter { it != null }
        return Pair<FeatureCollection, BoundingBox>(
            FeatureCollection.fromFeatures(features),
            BoundingBox.fromLngLats(westBound, southBound, eastBound, northBound)
        )
    }

    private fun handleGeometry(
        geometry: Geometry,
        property: String,
        propertyValue: String
    ): Feature? {
        if (geometry.type() == FeatureType.POINT) {
            val point = getPointFeature(geometry)
            point?.addStringProperty(property, propertyValue)
            return point
        } else if (geometry.type() == FeatureType.POLYGON) {
            val polygon = getPolygonFeature(geometry)
            polygon.addStringProperty(property, propertyValue)
            return polygon
        } else {
            return null
        }
    }

    private fun getPolygonFeature(geometry: Geometry): Feature {
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val pointList = ArrayList<Point>()
        sdkPolygon.forEach {
            it.forEach { coordinates ->
                val lat = coordinates[1]
                val lon = coordinates[0]

                checkBounds(lat, lon)

                pointList.add(Point.fromLngLat(lon, lat))
            }
        }
        val polygonArray = ArrayList<ArrayList<Point>>()
        polygonArray.add(pointList)
        val polygon = Polygon.fromLngLats(polygonArray as List<MutableList<Point>>)
        return Feature.fromGeometry(polygon)
    }

    private fun getPolygonPointFeature(geometry: Geometry): Feature {
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val lat = sdkPolygon[0][0][1]
        val lon = sdkPolygon[0][0][0]

        val point = if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
            Point.fromLngLat(lon, lat)
        } else {
            throw IllegalArgumentException("latitude or longitud have wrong values")
        }

        return Feature.fromGeometry(point)
    }

    private fun getPointFeature(geometry: Geometry): Feature? {
        val sdkPoint = GeometryHelper.getPoint(geometry)
        val lat = sdkPoint[1]
        val lon = sdkPoint[0]

        return if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
            checkBounds(lat, lon)
            val point = Point.fromLngLat(lon, lat)
            Feature.fromGeometry(point)
        } else {
            Timber.tag(javaClass.simpleName).d("INVALID COORDINATES lat :%s. lon: %s", lat, lon)
            null
        }
    }

    private fun checkBounds(lat: Double, lon: Double) {
        if (!boundsInt) {
            northBound = lat
            southBound = lat
            eastBound = lon
            westBound = lon
            boundsInt = true
        } else {
            if (northBound < lat) {
                northBound = lat
            }
            if (southBound > lat) {
                southBound = lat
            }
            if (eastBound < lon) {
                eastBound = lon
            }
            if (westBound > lon) {
                westBound = lon
            }
        }
    }
}
