package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection.Companion.EVENT_UID
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.dhis2.uicomponents.map.mocks.GeometryDummy.getGeometryAsPoint
import org.dhis2.uicomponents.map.model.EventUiComponentModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test

class MapTeiEventsToFeatureCollectionTest {

    private lateinit var mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection
    private val bounds: GetBoundingBox = mock()
    private val mapPointToFeature: MapPointToFeature = mock()
    private val mapPolygonToFeature: MapPolygonToFeature = mock()

    @Before
    fun setUp() {
        mapTeiEventsToFeatureCollection = MapTeiEventsToFeatureCollection(
            mapPointToFeature,
            mapPolygonToFeature,
            bounds
        )
    }

    @Test
    fun `Should map event models to point feature collection`() {
        val events = createEventsList()
        val eventFeaturePoint =
            Feature.fromGeometry(Point.fromLngLat(POINT_LONGITUDE, POINT_LATITUDE))

        whenever(mapPointToFeature.map(events[0].event.geometry()!!)) doReturn eventFeaturePoint

        val result = mapTeiEventsToFeatureCollection.map(events)
        val featureCollection = result.first.featureCollectionMap

        featureCollection.values.forEach { it ->
            val resultGeometry = it.features()?.get(0)?.geometry() as Point
            val resultUid = it.features()?.get(0)?.getStringProperty(EVENT_UID)

            assertThat(resultUid, `is`(EVENTUID))
            assertThat(resultGeometry.longitude(), `is`(POINT_LONGITUDE))
            assertThat(resultGeometry.latitude(), `is`(POINT_LATITUDE))
        }
    }

    private fun createEventsList(): List<EventUiComponentModel> {
        val event = EventUiComponentModel(
            "eventUid",
            Event.builder().uid(EVENTUID).geometry(
                getGeometryAsPoint("[$POINT_LONGITUDE, $POINT_LATITUDE]")
            ).build(),
            Enrollment.builder().uid("enrollmentUid").build(),
            ProgramStage.builder().uid("stageUid").displayName("stage").build(),
            Date(),
            linkedMapOf(),
            "image",
            "default"
        )
        return listOf(event)
    }

    companion object {
        const val EVENTUID = "eventUid"
        const val POINT_LONGITUDE = 43.34532
        const val POINT_LATITUDE = -23.98234
    }
}
