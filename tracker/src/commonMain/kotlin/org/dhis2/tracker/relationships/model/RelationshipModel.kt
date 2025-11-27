package org.dhis2.tracker.relationships.model

import java.util.Date

data class RelationshipModel(
    val relationshipUid: String,
    val relationshipState: String,
    val fromGeometry: RelationshipGeometry?,
    val toGeometry: RelationshipGeometry?,
    val direction: RelationshipDirection,
    val ownerUid: String,
    val ownerType: RelationshipOwnerType,
    val fromValues: List<Pair<String, String>>,
    val toValues: List<Pair<String, String>>,
    val fromImage: String?,
    val toImage: String?,
    val fromDefaultImageResource: Int,
    val toDefaultImageResource: Int,
    val ownerStyleIcon: String?,
    val ownerStyleColor: String?,
    val canBeOpened: Boolean = true,
    val toLastUpdated: Date? = null,
    val fromLastUpdated: Date? = null,
    val toDescription: String? = null,
    val fromDescription: String? = null,
) {
    fun displayRelationshipName(): String {
        val values =
            when (direction) {
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

    fun displayDescription(): String? =
        when (direction) {
            RelationshipDirection.FROM -> fromDescription
            RelationshipDirection.TO -> toDescription
        }

    fun displayLastUpdated(): Date? =
        when (direction) {
            RelationshipDirection.FROM -> fromLastUpdated
            RelationshipDirection.TO -> toLastUpdated
        }

    fun displayAttributes(): List<Pair<String, String>> =
        when (direction) {
            RelationshipDirection.FROM -> fromValues
            RelationshipDirection.TO -> toValues
        }.drop(1)

    fun firstMainValue(): String {
        val values =
            when (direction) {
                RelationshipDirection.FROM -> fromValues
                RelationshipDirection.TO -> toValues
            }
        return values
            .first()
            .second
            .firstOrNull()
            ?.toString() ?: ""
    }

    fun getPicturePath(): String =
        when (direction) {
            RelationshipDirection.FROM -> fromImage ?: ""
            RelationshipDirection.TO -> toImage ?: ""
        }

    fun displayGeometry(): RelationshipGeometry? =
        when (direction) {
            RelationshipDirection.FROM -> fromGeometry
            RelationshipDirection.TO -> toGeometry
        }
}

data class RelationshipGeometry(
    val featureType: String?,
    val coordinates: String?,
)

enum class RelationshipDirection {
    FROM,
    TO,
}

enum class RelationshipOwnerType {
    EVENT,
    TEI,
}
