package org.dhis2.uicomponents.map.geometry.line

import com.mapbox.geojson.LineString
import junit.framework.Assert.assertTrue
import org.dhis2.uicomponents.map.mocks.GeometryDummy
import org.dhis2.uicomponents.map.model.RelationshipDirection
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.dhis2.uicomponents.map.model.TeiMap
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MapLineToRelationshipFeatureTest {

    private val mapper = MapLineRelationshipToFeature()

    @Test
    fun `Should map line to feature`() {
        val relationshipModel = relationshipUiComponentModel()

        val result = mapper.map(relationshipModel)
        val line = result?.geometry() as LineString

        assertThat(line.type(), `is`(LINE_STRING))
        assertThat(line.coordinates()[0].longitude(), `is`(-30.0))
        assertThat(line.coordinates()[0].latitude(), `is`(11.0))
        assertThat(line.coordinates()[1].longitude(), `is`(-35.0))
        assertThat(line.coordinates()[1].latitude(), `is`(15.0))
    }

    @Test
    fun `Should not map line to feature`() {
        val relationshipModel = relationshipUiComponentModelWrongCoordinates()

        val result = mapper.map(relationshipModel)

        assertTrue(result == null)
    }

    private fun relationshipUiComponentModelWrongCoordinates(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointWrong()
        val geometryTo = GeometryDummy.getGeometryAsPointWrong()

        return RelationshipUiComponentModel(
            DISPLAY_NAME,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(TEIUID_FROM, geometryFrom, "", ""),
            TeiMap(TEIUID_TO, geometryTo, "", "")
        )
    }

    private fun relationshipUiComponentModel(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return RelationshipUiComponentModel(
            DISPLAY_NAME,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(TEIUID_FROM, geometryFrom, "", ""),
            TeiMap(TEIUID_TO, geometryTo, "", "")
        )
    }

    companion object {
        const val LINE_STRING = "LineString"
        const val DISPLAY_NAME = "displayName"
        const val TEIUID_FROM = "456"
        const val TEIUID_TO = "567"
        const val RELATIONSHIP_TYPE = "123"
    }
}