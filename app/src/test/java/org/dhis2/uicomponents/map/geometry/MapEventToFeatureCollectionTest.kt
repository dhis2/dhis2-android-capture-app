package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.event.Event
import org.junit.Before
import org.junit.Test

class MapEventToFeatureCollectionTest{

    private val mapGeometryToFeature: MapGeometryToFeature = mock()
    private val bounds: BoundsGeometry = mock()
    private lateinit var mapEventToFeatureCollection: MapEventToFeatureCollection


    @Before
    fun setup() {
        mapEventToFeatureCollection = MapEventToFeatureCollection(mapGeometryToFeature, bounds)
    }

    @Test
    fun `Should map events to feature collection`(){
        val(firstFeature, secondFeature) = createFeatures()
        val firstEvent = createEvent("[$FIRST_FEATURE_LONGITUDE, $FIRST_FEATURE_LATITUDE]", UID_FIRST_EVENT_VALUE)
        val secondEvent = createEvent(" [$SECOND_FEATURE_LONGITUDE, $SECOND_FEATURE_LATITUDE]", UID_SECOND_EVENT_VALUE)

        whenever(mapGeometryToFeature.map(any(), any(), any(), any())) doReturn firstFeature doReturn secondFeature

        val result = mapEventToFeatureCollection.map(listOf(firstEvent, secondEvent))

        val (featureList, bounding) = result
        val firstUid = featureList.features()?.get(0)?.getStringProperty(UID)
        val secondUid = featureList.features()?.get(1)?.getStringProperty(UID)
        val firstCoordinates = featureList.features()?.get(0)?.geometry() as Point
        val secondCoordinates = featureList.features()?.get(1)?.geometry() as Point

        assertThat(firstUid, `is`(UID_FIRST_EVENT_VALUE))
        assertThat(firstCoordinates.longitude(),`is`(FIRST_FEATURE_LONGITUDE))
        assertThat(firstCoordinates.latitude(),`is`(FIRST_FEATURE_LATITUDE))

        assertThat(secondUid, `is`(UID_SECOND_EVENT_VALUE))
        assertThat(secondCoordinates.longitude(),`is`(SECOND_FEATURE_LONGITUDE))
        assertThat(secondCoordinates.latitude(),`is`(SECOND_FEATURE_LATITUDE))

        assertThat(bounding.north(), `is`(0.0))
        assertThat(bounding.south(), `is`(0.0))
        assertThat(bounding.west(), `is`(0.0))
        assertThat(bounding.east(), `is`(0.0))
    }

    private fun createFeatures() : Pair<Feature, Feature> {
        return Pair(Feature.fromGeometry(Point.fromLngLat(FIRST_FEATURE_LONGITUDE, FIRST_FEATURE_LATITUDE))
                .also { it.addStringProperty(UID, UID_FIRST_EVENT_VALUE) },
                Feature.fromGeometry(Point.fromLngLat(SECOND_FEATURE_LONGITUDE, SECOND_FEATURE_LATITUDE))
                        .also {it.addStringProperty(UID, UID_SECOND_EVENT_VALUE)})
    }

    private fun createEvent(coordinates: String, uid:String) : Event {
        val geometry = Geometry.builder()
                .coordinates(coordinates)
                .type(FeatureType.POINT).build()

        return Event.builder().geometry(geometry).uid(uid).build();
    }

    companion object {
        const val UID = "uid"
        const val UID_FIRST_EVENT_VALUE ="123"
        const val UID_SECOND_EVENT_VALUE ="456"
        const val FIRST_FEATURE_LONGITUDE = -12.3843383789062
        const val FIRST_FEATURE_LATITUDE = 8.20061648260833
        const val SECOND_FEATURE_LONGITUDE = -12.2621154785156
        const val SECOND_FEATURE_LATITUDE = 8.20061648260833
    }
}