package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.dhislogic.CoordinateAttributeInfo
import org.dhis2.data.dhislogic.CoordinateDataElementInfo
import org.dhis2.uicomponents.map.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MapCoordinateFieldToFeatureCollectionTest {

    private lateinit var mapper: MapCoordinateFieldToFeatureCollection
    private val mapCoordinateFieldToFeature: MapCoordinateFieldToFeature = mock()

    @Before
    fun setUp() {
        mapper = MapCoordinateFieldToFeatureCollection(mapCoordinateFieldToFeature)
    }

    @Test
    fun `Should map data element list to feature collection map`() {
        whenever(
            mapCoordinateFieldToFeature.map(any<CoordinateDataElementInfo>())
        ) doReturnConsecutively mockedFeatures()

        mapper.map(mockedDataElementInfoList()).apply {
            assertTrue(size == 2)
            assertTrue(keys.containsAll(arrayListOf("deName", "de2Name")))
        }
    }

    @Test
    fun `Should map attribute list to feature collection map`() {
        whenever(
            mapCoordinateFieldToFeature.map(any<CoordinateAttributeInfo>())
        ) doReturnConsecutively mockedFeatures()

        mapper.map(mockedAttributeInfoList()).apply {
            assertTrue(size == 2)
            assertTrue(keys.containsAll(arrayListOf("attrName", "attr2Name")))
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

    private fun mockedAttributeInfoList(): List<CoordinateAttributeInfo> {
        return listOf(
            CoordinateAttributeInfo(
                TrackedEntityInstance.builder().uid("teiUid").build(),
                TrackedEntityAttribute.builder().uid("attrUid").displayFormName("attrName").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build()
            ),
            CoordinateAttributeInfo(
                TrackedEntityInstance.builder().uid("teiUid").build(),
                TrackedEntityAttribute.builder().uid("attr2Uid")
                    .displayFormName("attr2Name").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build()
            ),
            CoordinateAttributeInfo(
                TrackedEntityInstance.builder().uid("tei2Uid").build(),
                TrackedEntityAttribute.builder().uid("attrUid").displayFormName("attrName").build(),
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
