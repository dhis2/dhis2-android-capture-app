package org.dhis2.tracker.relationships.model

data class RelationshipSection(
    val uid: String,
    val title: String,
    val relationships: List<RelationshipModel>,
    val side: RelationshipConstraintSide,
    val entityToAdd: String?,
)
