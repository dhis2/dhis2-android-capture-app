package org.dhis2.tracker.relationships.model

import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipType
import java.util.Date

data class RelationshipModel(
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
    val ownerStyle: ObjectStyle?,
    val canBeOpened: Boolean = true,
    val toLastUpdated: Date? = null,
    val fromLastUpdated: Date? = null,
    val toDescription: String? = null,
    val fromDescription: String? = null,
) {
    fun displayRelationshipName(): String {
        val values = when (direction) {
            RelationshipDirection.FROM -> fromValues
            RelationshipDirection.TO -> toValues
        }
        return when {
            values.isNotEmpty() -> {
                val firstPair = values.first()
                val label = firstPair.first
                val value = firstPair.second
                if (value.isNotEmpty()) "$label: $value" else label
            }

            else -> "-"
        }
    }

    fun displayDescription(): String? {
        return when (direction) {
            RelationshipDirection.FROM -> fromDescription
            RelationshipDirection.TO -> toDescription
        }
    }

    fun displayLastUpdated(): Date? {
        return when (direction) {
            RelationshipDirection.FROM -> fromLastUpdated
            RelationshipDirection.TO -> toLastUpdated
        }
    }

    fun displayAttributes(): List<Pair<String, String>> {
        return when (direction) {
            RelationshipDirection.FROM -> fromValues
            RelationshipDirection.TO -> toValues
        }.drop(1)
    }

    fun firstMainValue(): String {
        val values = when (direction) {
            RelationshipDirection.FROM -> fromValues
            RelationshipDirection.TO -> toValues
        }
        return values.first().second.firstOrNull()
            ?.toString() ?: ""
    }

    fun getPicturePath(): String {
        return when (direction) {
            RelationshipDirection.FROM -> fromImage ?: ""
            RelationshipDirection.TO -> toImage ?: ""
        }
    }

    fun displayGeometry(): Geometry? {
        return when (direction) {
            RelationshipDirection.FROM -> fromGeometry
            RelationshipDirection.TO -> toGeometry
        }
    }
}

enum class RelationshipDirection {
    FROM, TO
}

enum class RelationshipOwnerType {
    EVENT, TEI
}
