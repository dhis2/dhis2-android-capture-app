package org.dhis2.uicomponents.map.geometry

import org.dhis2.maps.model.RelationshipDirection
import org.dhis2.uicomponents.map.mocks.GeometryDummy.FROM_COORDINATES
import org.dhis2.uicomponents.map.mocks.GeometryDummy.TO_COORDINATES
import org.dhis2.uicomponents.map.mocks.RelationshipViewModelDummy
import org.dhis2.uicomponents.map.mocks.RelationshipViewModelDummy.DISPLAY_NAME
import org.dhis2.uicomponents.map.mocks.RelationshipViewModelDummy.EMPTY
import org.dhis2.uicomponents.map.mocks.RelationshipViewModelDummy.UID
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MapRelationshipToRelationshipMapModelTest {

    private val mapper = org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel()

    @Test
    fun `Should map relationship view model to Ui component model`() {
        val relationshipViewModelFull = RelationshipViewModelDummy.getViewModelTypeFull()
        val relationshipsViewModelNoCoordinates =
            RelationshipViewModelDummy.getViewModelTypeNoCoordinates()

        val result =
            mapper.mapList(listOf(relationshipViewModelFull, relationshipsViewModelNoCoordinates))

        assertThat(result.size, `is`(1))
        assertThat(result[0].displayName, `is`(DISPLAY_NAME))
        assertThat(result[0].bidirectional, `is`(false))
        assertThat(result[0].relationshipTypeUid, `is`(UID))
        assertThat(result[0].direction, `is`(RelationshipDirection.FROM))
        assertThat(result[0].from.defaultImage, `is`(-1))
        assertThat(result[0].from.image, `is`(""))
        assertThat(result[0].from.geometry?.coordinates(), `is`(FROM_COORDINATES))
        assertThat(result[0].to.geometry?.coordinates(), `is`(TO_COORDINATES))
        assertThat(result[0].to.defaultImage, `is`(-1))
        assertThat(result[0].to.image, `is`(EMPTY))
    }
}
