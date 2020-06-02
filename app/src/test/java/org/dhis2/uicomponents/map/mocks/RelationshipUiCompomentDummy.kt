package org.dhis2.uicomponents.map.mocks

import org.dhis2.uicomponents.map.model.RelationshipDirection
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.dhis2.uicomponents.map.model.TeiMap

object RelationshipUiCompomentDummy {

     fun relationshipUiComponentModelWrongCoordinates(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointWrong()
        val geometryTo = GeometryDummy.getGeometryAsPointWrong()

        return RelationshipUiComponentModel(
            DISPLAY_NAME_FIRST,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(TEIUID_FROM, geometryFrom, "", ""),
            TeiMap(TEIUID_TO, geometryTo, "", "")
        )
    }

    fun relationshipUiComponentModel(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return RelationshipUiComponentModel(
            DISPLAY_NAME_FIRST,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(TEIUID_FROM, geometryFrom, "", ""),
            TeiMap(TEIUID_TO, geometryTo, "", "")
        )
    }

    fun relationshipUiComponentModelSecond(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return RelationshipUiComponentModel(
            DISPLAY_NAME_SECOND,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(TEIUID_FROM, geometryFrom, "", ""),
            TeiMap(TEIUID_TO, geometryTo, "", "")
        )
    }

    const val LINE_STRING = "LineString"
    const val DISPLAY_NAME_FIRST = "displayNameFirst"
    const val DISPLAY_NAME_SECOND = "displayNameSecond"
    const val TEIUID_FROM = "456"
    const val TEIUID_TO = "567"
    const val RELATIONSHIP_TYPE = "123"
}
