package org.dhis2.commons.data

import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipType

data class RelationshipViewModel(
    val relationship: Relationship,
    val fromGeometry: Geometry?,
    val toGeometry: Geometry?,
    val relationshipType: RelationshipType,
    val direction: RelationshipDirection,
    val ownerUid: String,
    val ownerType: RelationshipOwnerType,
    val fromValues: List<Pair<String, String>>,
    val toValues: List<Pair<String, String>>,
    val fromImage: String?,
    val toImage: String?,
    val fromDefaultImageResource: Int,
    val toDefaultImageResource: Int,
    val ownerDefaultColorResource: MetadataIconData,
    val canBeOpened: Boolean = true,
) {
    fun displayRelationshipName(): String {
        val values = when (direction) {
            RelationshipDirection.FROM -> fromValues
            RelationshipDirection.TO -> toValues
        }
        return when {
            values.size > 1 -> "${values[0].second} ${values[1].second}"
            values.size == 1 -> values[0].second
            else -> "-"
        }
    }

    fun displayRelationshipTypeName(): String {
        return when (direction) {
            RelationshipDirection.FROM -> relationshipType.toFromName()
            RelationshipDirection.TO -> relationshipType.fromToName()
        } ?: relationshipType.displayName() ?: "-"
    }

    fun displayImage(): Pair<String?, Int> {
        return when (direction) {
            RelationshipDirection.FROM -> Pair(fromImage, fromDefaultImageResource)
            RelationshipDirection.TO -> Pair(toImage, toDefaultImageResource)
        }
    }

    fun isEvent(): Boolean {
        return ownerType == RelationshipOwnerType.EVENT
    }

    fun isFrom(): Boolean {
        return direction == RelationshipDirection.FROM
    }
}

enum class RelationshipDirection {
    FROM, TO
}

enum class RelationshipOwnerType {
    EVENT, TEI
}
