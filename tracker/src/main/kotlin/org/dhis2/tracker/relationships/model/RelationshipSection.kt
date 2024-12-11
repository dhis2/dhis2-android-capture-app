package org.dhis2.tracker.relationships.model

import org.hisp.dhis.android.core.relationship.RelationshipType

data class RelationshipSection(
    val title: String,
    val relationships: List<RelationshipItem>,
    val creationTEITypeUid: String?,
    val relationshipType: RelationshipType,
    val direction: RelationshipDirection,
) {
    fun canAddRelationship(): Boolean = creationTEITypeUid != null
}
