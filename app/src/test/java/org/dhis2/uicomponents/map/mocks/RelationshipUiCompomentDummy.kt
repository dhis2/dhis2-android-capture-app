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
            UID_FIRST,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(
                TEIUID_FROM,
                geometryFrom,
                "",
                -1,
                ""
            ),
            TeiMap(
                TEIUID_TO,
                geometryTo,
                "",
                -1,
                ""
            )
        )
    }

    fun relationshipUiComponentModel(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return RelationshipUiComponentModel(
            DISPLAY_NAME_FIRST,
            UID_FIRST,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(
                TEIUID_FROM,
                geometryFrom,
                "",
                -1,
                ""
            ),
            TeiMap(
                TEIUID_TO,
                geometryTo,
                "",
                -1,
                ""
            )
        )
    }

    fun relationshipUiComponentModelSecond(): RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return RelationshipUiComponentModel(
            DISPLAY_NAME_SECOND,
            UID_SECOND,
            RELATIONSHIP_TYPE,
            RelationshipDirection.FROM,
            false,
            TeiMap(TEIUID_FROM, geometryFrom, "", -1, ""),
            TeiMap(TEIUID_TO, geometryTo, "", -1, "")
        )
    }

    const val LINE_STRING = "LineString"
    const val DISPLAY_NAME_FIRST = "displayNameFirst"
    const val UID_FIRST = "uid"
    const val UID_SECOND = "uid2"
    const val DISPLAY_NAME_SECOND = "displayNameSecond"
    const val TEIUID_FROM = "456"
    const val TEIUID_TO = "567"
    const val RELATIONSHIP_TYPE = "123"
}
