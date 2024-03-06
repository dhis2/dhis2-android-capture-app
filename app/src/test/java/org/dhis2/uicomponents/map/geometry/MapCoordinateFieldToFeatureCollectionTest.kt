package org.dhis2.uicomponents.map.geometry

import org.dhis2.maps.geometry.mapper.featurecollection.MapAttributeToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapDataElementToFeature
import org.dhis2.maps.utils.CoordinateAttributeInfo
import org.dhis2.maps.utils.CoordinateDataElementInfo
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MapCoordinateFieldToFeatureCollectionTest {

    private lateinit var mapper: MapCoordinateFieldToFeatureCollection
    private val attributeToFeatureMapper: MapAttributeToFeature = mock()
    private val dataElementToFeatureMapper: MapDataElementToFeature = mock()

    @Before
    fun setUp() {
        mapper =
            MapCoordinateFieldToFeatureCollection(
                dataElementToFeatureMapper,
                attributeToFeatureMapper
            )
    }

    @Test
    fun `Should map data element list to feature collection map`() {
        mapper.map(mockedDataElementInfoList())
        verify(dataElementToFeatureMapper).mapDataElement(any())
    }

    @Test
    fun `Should map attribute list to feature collection map`() {
        mapper.map(mockedAttributeInfoList())
        verify(attributeToFeatureMapper).mapAttribute(any())
    }

    @Test
    fun `Should return empty map`() {
        val result = mapper.map(emptyList())
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
}
