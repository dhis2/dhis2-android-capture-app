package org.dhis2.uicomponents.map.geometry.mapper.featurecollection

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapAttributeToFeature
import org.dhis2.maps.utils.CoordinateAttributeInfo
import org.dhis2.uicomponents.map.geometry.MapEventToFeatureCollectionTest
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapAttributeToFeatureTest {
    private val mapCoordinateFieldToFeature: MapCoordinateFieldToFeature = mock()
    private lateinit var attributeMapper: MapAttributeToFeature

    @Before
    fun setUp() {
        attributeMapper = MapAttributeToFeature(mapCoordinateFieldToFeature)
    }

    @Test
    fun `Should return empty map`() {
        val result = attributeMapper.mapAttribute(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Should return map of feature collections`() {
        whenever(
            mapCoordinateFieldToFeature.map(any<CoordinateAttributeInfo>()),
        ) doReturnConsecutively mockedFeatures()
        val result = attributeMapper.mapAttribute(mockedAttributeInfoList())
        assertTrue(result.isNotEmpty())
        assertTrue(result.containsKey("attrName"))
        assertTrue(result.containsKey("attr2Name"))
        assertTrue(result["attrName"] != null)
        assertTrue(result["attr2Name"] != null)
    }

    @Test
    fun `Should return empty map if features are null`() {
        whenever(mapCoordinateFieldToFeature.map(any<CoordinateAttributeInfo>())) doReturn null
        val result = attributeMapper.mapAttribute(mockedAttributeInfoList())
        assertTrue(result.isEmpty())
    }

    private fun mockedAttributeInfoList(): List<CoordinateAttributeInfo> {
        return listOf(
            CoordinateAttributeInfo(
                TrackedEntityInstance.builder().uid("teiUid").build(),
                TrackedEntityAttribute.builder().uid("attrUid").displayFormName("attrName").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build(),
            ),
            CoordinateAttributeInfo(
                TrackedEntityInstance.builder().uid("teiUid").build(),
                TrackedEntityAttribute.builder().uid("attr2Uid")
                    .displayFormName("attr2Name").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build(),
            ),
            CoordinateAttributeInfo(
                TrackedEntityInstance.builder().uid("tei2Uid").build(),
                TrackedEntityAttribute.builder().uid("attrUid").displayFormName("attrName").build(),
                Geometry.builder().coordinates("[0, 0]").type(FeatureType.POINT).build(),
            ),
        )
    }

    private fun mockedFeatures(): List<Feature> {
        return listOf(
            Feature.fromGeometry(
                Point.fromLngLat(
                    MapEventToFeatureCollectionTest.FIRST_FEATURE_LONGITUDE,
                    MapEventToFeatureCollectionTest.FIRST_FEATURE_LATITUDE,
                ),
            ).also {
                it.addStringProperty(
                    MapEventToFeatureCollectionTest.UID,
                    MapEventToFeatureCollectionTest.UID_FIRST_EVENT_VALUE,
                )
            },
            Feature.fromGeometry(
                Point.fromLngLat(
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LONGITUDE,
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LATITUDE,
                ),
            ).also {
                it.addStringProperty(
                    MapEventToFeatureCollectionTest.UID,
                    MapEventToFeatureCollectionTest.UID_SECOND_EVENT_VALUE,
                )
            },
            Feature.fromGeometry(
                Point.fromLngLat(
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LONGITUDE,
                    MapEventToFeatureCollectionTest.SECOND_FEATURE_LATITUDE,
                ),
            ).also {
                it.addStringProperty(
                    MapEventToFeatureCollectionTest.UID,
                    MapEventToFeatureCollectionTest.UID_SECOND_EVENT_VALUE,
                )
            },
        )
    }
}
