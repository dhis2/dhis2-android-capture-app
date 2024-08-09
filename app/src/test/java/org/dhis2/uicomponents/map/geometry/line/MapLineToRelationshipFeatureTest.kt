package org.dhis2.uicomponents.map.geometry.line

import com.mapbox.geojson.LineString
import junit.framework.Assert.assertTrue
import org.dhis2.uicomponents.map.mocks.RelationshipUiCompomentDummy.LINE_STRING
import org.dhis2.uicomponents.map.mocks.RelationshipUiCompomentDummy.relationshipUiComponentModel
import org.dhis2.uicomponents.map.mocks.RelationshipUiCompomentDummy.relationshipUiComponentModelWrongCoordinates
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MapLineToRelationshipFeatureTest {

    private val mapper = org.dhis2.maps.geometry.line.MapLineRelationshipToFeature()

    @Test
    fun `Should map line to feature`() {
        val relationshipModel = relationshipUiComponentModel()

        val result = with(relationshipModel) {
            mapper.map(
                from.geometry!!,
                to.geometry!!,
            )
        }
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

        val result = with(relationshipModel) {
            mapper.map(
                from.geometry!!,
                to.geometry!!,
            )
        }

        assertTrue(result == null)
    }
}
