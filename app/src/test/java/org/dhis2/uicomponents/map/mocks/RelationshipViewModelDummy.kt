package org.dhis2.uicomponents.map.mocks

import java.text.ParseException
import java.util.Date
import org.dhis2.commons.data.RelationshipDirection
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.data.RelationshipViewModel
import org.hisp.dhis.android.core.common.BaseIdentifiableObject
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipType

object RelationshipViewModelDummy {

    fun getViewModelTypeFull(): RelationshipViewModel {
        val relationship = Relationship
            .builder()
            .created(CREATED)
            .lastUpdated(LAST_UPDATED)
            .uid(UID)
            .name(NAME)
            .relationshipType(RELATIONSHIP_TYPE)
            .build()

        val relationshipType = RelationshipType
            .builder()
            .id(2L)
            .uid(UID)
            .displayName(DISPLAY_NAME)
            .bidirectional(false)
            .build()

        val geometryFrom = GeometryDummy.getGeometryAsPointFrom()
        val geometryTo = GeometryDummy.getGeometryAsPointTo()

        return RelationshipViewModel(
            relationship,
            geometryFrom,
            geometryTo,
            relationshipType,
            RelationshipDirection.FROM,
            UID,
            RelationshipOwnerType.TEI,
            emptyList(),
            emptyList(),
            "",
            "",
            -1,
            -1,
            -1
        )
    }

    fun getViewModelTypeNoCoordinates(): RelationshipViewModel {
        val relationship = Relationship
            .builder()
            .created(CREATED)
            .lastUpdated(LAST_UPDATED)
            .uid(UID)
            .name(NAME)
            .relationshipType(RELATIONSHIP_TYPE)
            .build()

        val relationshipType = RelationshipType
            .builder()
            .id(2L)
            .uid(UID)
            .displayName(DISPLAY_NAME)
            .bidirectional(false)
            .build()

        return RelationshipViewModel(
            relationship,
            null,
            null,
            relationshipType,
            RelationshipDirection.FROM,
            UID,
            RelationshipOwnerType.TEI,
            emptyList(),
            emptyList(),
            "",
            "",
            -1,
            -1,
            -1
        )
    }

    const val UID = "test_uid"
    const val RELATIONSHIP_TYPE = "relationship_type"
    const val NAME = "test_name"
    const val DISPLAY_NAME = "Test-to-test"
    const val EMPTY = ""

    private const val CREATED_STR = "2012-10-20T18:20:27.132"
    private const val LAST_UPDATED_STR = "2017-12-20T15:08:27.882"
    private val CREATED = parseDate(CREATED_STR)
    private val LAST_UPDATED = parseDate(LAST_UPDATED_STR)

    private fun parseDate(dateStr: String): Date {
        return try {
            BaseIdentifiableObject.DATE_FORMAT.parse(dateStr)
        } catch (e: ParseException) {
            Date()
        }
    }
}
