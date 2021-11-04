package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.dhislogic.CoordinateDataElementInfo
import org.dhis2.uicomponents.map.geometry.MapEventToFeatureCollectionTest
import org.dhis2.android_maps.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MapDataElementToFeatureTest {
    private val mapCoordinateFieldToFeature: org.dhis2.android_maps.geometry.mapper.feature.MapCoordinateFieldToFeature = mock()
    private lateinit var dataElementMapper: org.dhis2.android_maps.geometry.mapper.featurecollection.MapDataElementToFeature

    @Before
    fun setUp() {
        dataElementMapper =
            org.dhis2.android_maps.geometry.mapper.featurecollection.MapDataElementToFeature(
                mapCoordinateFieldToFeature
            )
    }

    @Test
    fun `Should return empty map`() {
        val result = dataElementMapper.mapDataElement(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Should return map of feature collections`() {
        whenever(
            mapCoordinateFieldToFeature.map(
                any<CoordinateDataElementInfo>()
            )
        ) doReturnConsecutively mockedFeatures()
        val result = dataElementMapper.mapDataElement(mockedDataElementInfoList())
        assertTrue(result.isNotEmpty())
        assertTrue(result.containsKey("deName"))
        assertTrue(result.containsKey("de2Name"))
        assertTrue(result["deName"] != null)
        assertTrue(result["de2Name"] != null)
    }

    @Test
    fun `Should return empty map if features are null`() {
        whenever(mapCoordinateFieldToFeature.map(any<CoordinateDataElementInfo>())) doReturn null
        val result = dataElementMapper.mapDataElement(mockedDataElementInfoList())
        assertTrue(result.isEmpty())
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
