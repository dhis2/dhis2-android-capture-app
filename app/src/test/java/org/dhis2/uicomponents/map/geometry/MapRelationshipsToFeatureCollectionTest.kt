package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox
import org.dhis2.uicomponents.map.geometry.line.MapLineRelationshipToFeature
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.mocks.RelationshipUiCompomentDummy.relationshipUiComponentModel
import org.dhis2.uicomponents.map.mocks.RelationshipUiCompomentDummy.relationshipUiComponentModelSecond
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.junit.Before
import org.junit.Test

class MapRelationshipsToFeatureCollectionTest {

    private val mapLineToFeature: MapLineRelationshipToFeature = mock()
    private val mapPointToFeature: MapPointToFeature = mock()
    private val bounds: GetBoundingBox = mock()
    private lateinit var mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection

    @Before
    fun setup() {
        mapRelationshipsToFeatureCollection =
            MapRelationshipsToFeatureCollection(mapLineToFeature, mapPointToFeature, bounds)
    }

    @Test
    fun `Should map relationshipUiComponents to featureCollection and boundingBox`() {
        val firstRelationship = relationshipUiComponentModel()
        val secondRelationship = relationshipUiComponentModelSecond()
        val relationshipsModel =
            listOf(relationshipUiComponentModel(), relationshipUiComponentModelSecond())

        whenever(mapLineToFeature.map(firstRelationship)) doReturn getLineFeature(firstRelationship)
        whenever(mapLineToFeature.map(secondRelationship)) doReturn getLineFeature(secondRelationship)
        whenever(mapPointToFeature.map(firstRelationship.from.geometry!!)) doReturn getPointFromFeature(firstRelationship)
        whenever(mapPointToFeature.map(secondRelationship.from.geometry!!)) doReturn getPointFromFeature(secondRelationship)
        whenever(mapPointToFeature.map(firstRelationship.To.geometry!!)) doReturn getPointToFeature(firstRelationship)
        whenever(mapPointToFeature.map(secondRelationship.To.geometry!!)) doReturn getPointToFeature(secondRelationship)
        whenever(bounds.getEnclosingBoundingBox(any())) doReturn BoundingBox.fromLngLats(
            0.0,
            0.0,
            0.0,
            0.0
        )

        val result = mapRelationshipsToFeatureCollection.map(relationshipsModel)
        assertThat(result.first.size, `is`(2))

        val relationshipFirstType = result.first[FIRST_RELATIONSHIP_TYPE]
        assertThat(relationshipFirstType?.features()?.size, `is`(3))

        val line = relationshipFirstType?.features()?.get(0)?.geometry() as LineString
        assertThat(line.coordinates()[0].longitude(), `is`(FROM_LONGITUDE))
        assertThat(line.coordinates()[0].latitude(), `is`(FROM_LATITUDE))
        assertThat(line.coordinates()[1].longitude(), `is`(TO_LONGITUDE))
        assertThat(line.coordinates()[1].latitude(), `is`(TO_LATITUDE))

        val pointFrom = relationshipFirstType.features()?.get(1)?.geometry() as Point
        assertThat(pointFrom.longitude(), `is`(FROM_LONGITUDE))
        assertThat(pointFrom.latitude(), `is`(FROM_LATITUDE))

        val pointTo = relationshipFirstType.features()?.get(2)?.geometry() as Point
        assertThat(pointTo.longitude(), `is`(TO_LONGITUDE))
        assertThat(pointTo.latitude(), `is`(TO_LATITUDE))


        val relationshipSecondType = result.first[SECOND_RELATIONSHIP_TYPE]
        assertThat(relationshipSecondType?.features()?.size, `is`(3))

        val lineSecondType = relationshipSecondType?.features()?.get(0)?.geometry() as LineString
        assertThat(lineSecondType.coordinates()[0].longitude(), `is`(FROM_LONGITUDE))
        assertThat(lineSecondType.coordinates()[0].latitude(), `is`(FROM_LATITUDE))
        assertThat(lineSecondType.coordinates()[1].longitude(), `is`(TO_LONGITUDE))
        assertThat(lineSecondType.coordinates()[1].latitude(), `is`(TO_LATITUDE))

        val pointFromSecondType = relationshipSecondType.features()?.get(1)?.geometry() as Point
        assertThat(pointFromSecondType.longitude(), `is`(FROM_LONGITUDE))
        assertThat(pointFromSecondType.latitude(), `is`(FROM_LATITUDE))

        val pointToSecondType = relationshipSecondType.features()?.get(2)?.geometry() as Point
        assertThat(pointToSecondType.longitude(), `is`(TO_LONGITUDE))
        assertThat(pointToSecondType.latitude(), `is`(TO_LATITUDE))
    }

    private fun getLineFeature(model: RelationshipUiComponentModel): Feature {
        val coordinates1 = GeometryHelper.getPoint(model.from.geometry!!)
        val coordinates2 = GeometryHelper.getPoint(model.To.geometry!!)
        return Feature.fromGeometry(
            LineString.fromLngLats(
                listOf(
                    Point.fromLngLat(
                        coordinates1[0],
                        coordinates1[1]
                    ),
                    Point.fromLngLat(
                        coordinates2[0],
                        coordinates2[1]
                    )
                )
            )
        )
    }

    private fun getPointFromFeature(model: RelationshipUiComponentModel): Feature{
        val coordinates1 = GeometryHelper.getPoint(model.from.geometry!!)

        val point = Point.fromLngLat(coordinates1[0], coordinates1[1])
        return Feature.fromGeometry(point)
    }

    private fun getPointToFeature(model: RelationshipUiComponentModel): Feature{
        val coordinates1 = GeometryHelper.getPoint(model.To.geometry!!)

        val point = Point.fromLngLat(coordinates1[0], coordinates1[1])
        return Feature.fromGeometry(point)
    }

    companion object {
        const val FIRST_RELATIONSHIP_TYPE = "displayNameFirst"
        const val SECOND_RELATIONSHIP_TYPE = "displayNameSecond"
        const val FROM_LONGITUDE = -30.0
        const val FROM_LATITUDE = 11.0
        const val TO_LONGITUDE = -35.0
        const val TO_LATITUDE = 15.0
    }
}
