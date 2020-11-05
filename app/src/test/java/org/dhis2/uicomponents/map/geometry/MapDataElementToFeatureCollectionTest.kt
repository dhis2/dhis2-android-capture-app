package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.dhislogic.CoordinateDataElementInfo
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapDataElementToFeatureCollection
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MapDataElementToFeatureCollectionTest {

    private lateinit var mapper: MapDataElementToFeatureCollection
    private val mapGeometryToFeature: MapGeometryToFeature = mock()
    private val bounds: BoundsGeometry = mock()

    @Before
    fun setUp() {
        mapper = MapDataElementToFeatureCollection(mapGeometryToFeature, bounds)
    }

    @Test
    fun `Should map list to feature collection map`() {
        whenever(
            mapGeometryToFeature.map(any(), any(), any())
        ) doReturnConsecutively mockedFeatures()

        mapper.map(mockedDataElementInfoList()).apply {
            assertTrue(size == 2)
            assertTrue(keys.containsAll(arrayListOf("deName", "de2Name")))
        }
    }

    private fun mockedDataElementInfoList(): List<CoordinateDataElementInfo> {
        return listOf(
            CoordinateDataElementInfo(
                Event.builder().uid("eventUid").programStage("stageUid").build(),
                ProgramStage.builder().uid("stageUid").displayName("stageName").build(),
                DataElement.builder().uid("deUid").displayFormName("deName").build(),
                Enrollment.builder().uid("enrollmentUid").trackedEntityInstance("teiUid").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build()
            ),
            CoordinateDataElementInfo(
                Event.builder().uid("eventUid").programStage("stageUid").build(),
                ProgramStage.builder().uid("stageUid").displayName("stageName").build(),
                DataElement.builder().uid("de2Uid").displayFormName("de2Name").build(),
                Enrollment.builder().uid("enrollmentUid").trackedEntityInstance("teiUid").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build()
            ),
            CoordinateDataElementInfo(
                Event.builder().uid("event2Uid").programStage("stageUid").build(),
                ProgramStage.builder().uid("stageUid").displayName("stageName").build(),
                DataElement.builder().uid("deUid").displayFormName("deName").build(),
                Enrollment.builder().uid("enrollmentUid").trackedEntityInstance("teiUid").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build()
            )
        )
    }

    private fun mockedFeatures(): List<Feature> {
        return listOf(
            Feature.fromGeometry(
                Point.fromLngLat(
                    MapEventToFeatureCollectionTest.FIRST_FEATURE_LONGITUDE,
                    MapEventToFeatureCollectionTest.FIRST_FEATURE_LATITUDE
                )
            ).also {
                it.addStringProperty(
                    MapEventToFeatureCollectionTest.UID,
                    MapEventToFeatureCollectionTest.UID_FIRST_EVENT_VALUE
                )
            },
            Feature.fromGeometry(
                Point.fromLngLat(
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LONGITUDE,
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LATITUDE
                )
            ).also {
                it.addStringProperty(
                    MapEventToFeatureCollectionTest.UID,
                    MapEventToFeatureCollectionTest.UID_SECOND_EVENT_VALUE
                )
            },
            Feature.fromGeometry(
                Point.fromLngLat(
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LONGITUDE,
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LATITUDE
                )
            ).also {
                it.addStringProperty(
                    MapEventToFeatureCollectionTest.UID,
                    MapEventToFeatureCollectionTest.UID_SECOND_EVENT_VALUE
                )
            }
        )
    }
}
