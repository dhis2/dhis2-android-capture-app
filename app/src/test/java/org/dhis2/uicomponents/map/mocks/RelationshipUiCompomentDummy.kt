package org.dhis2.uicomponents.map.mocks

import org.dhis2.android_maps.model.RelationshipDirection
import org.dhis2.android_maps.model.RelationshipUiComponentModel
import org.dhis2.android_maps.model.TeiMap

object RelationshipUiCompomentDummy {

    fun relationshipUiComponentModelWrongCoordinates(): org.dhis2.android_maps.model.RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointWrong()
        val geometryTo = GeometryDummy.getGeometryAsPointWrong()

        return org.dhis2.android_maps.model.RelationshipUiComponentModel(
            DISPLAY_NAME_FIRST,
            UID_FIRST,
            RELATIONSHIP_TYPE,
            org.dhis2.android_maps.model.RelationshipDirection.FROM,
            false,
            org.dhis2.android_maps.model.TeiMap(
                TEIUID_FROM,
                geometryFrom,
                "",
                -1,
                ""
            ),
            org.dhis2.android_maps.model.TeiMap(
                TEIUID_TO,
                geometryTo,
                "",
                -1,
                ""
            )
        )
    }

    fun relationshipUiComponentModel(): org.dhis2.android_maps.model.RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return org.dhis2.android_maps.model.RelationshipUiComponentModel(
            DISPLAY_NAME_FIRST,
            UID_FIRST,
            RELATIONSHIP_TYPE,
            org.dhis2.android_maps.model.RelationshipDirection.FROM,
            false,
            org.dhis2.android_maps.model.TeiMap(
                TEIUID_FROM,
                geometryFrom,
                "",
                -1,
                ""
            ),
            org.dhis2.android_maps.model.TeiMap(
                TEIUID_TO,
                geometryTo,
                "",
                -1,
                ""
            )
        )
    }

    fun relationshipUiComponentModelSecond(): org.dhis2.android_maps.model.RelationshipUiComponentModel {
        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return org.dhis2.android_maps.model.RelationshipUiComponentModel(
            DISPLAY_NAME_SECOND,
            UID_SECOND,
            RELATIONSHIP_TYPE,
            org.dhis2.android_maps.model.RelationshipDirection.FROM,
            false,
            org.dhis2.android_maps.model.TeiMap(TEIUID_FROM, geometryFrom, "", -1, ""),
            org.dhis2.android_maps.model.TeiMap(TEIUID_TO, geometryTo, "", -1, "")
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
