package org.dhis2.tracker.relationships.model

import org.hisp.dhis.android.core.relationship.RelationshipType

data class RelationshipSection(
    val relationships: List<RelationShipItem>,
    val teiTypeUid: String,
    val relationshipType: RelationshipType,
)
