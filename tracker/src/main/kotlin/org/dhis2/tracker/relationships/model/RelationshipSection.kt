package org.dhis2.tracker.relationships.model

import org.hisp.dhis.android.core.relationship.RelationshipType

data class RelationshipSection(
    val title: String,
    val relationships: List<RelationshipItem>,
    val teiTypeUid: String?,
    val relationshipType: RelationshipType,
) {
    fun canAddRelationship(): Boolean = teiTypeUid != null
}
