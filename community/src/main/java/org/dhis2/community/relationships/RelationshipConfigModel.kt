package org.dhis2.community.relationships

data class RelationshipConfig(
    val relationships: List<Relationship>
)

data class Relationship(
    val access: Access,
    val description: String,
    val view: View
)

data class Access(
    val targetProgramUid: String,
    val targetRelationshipUid: String,
    val targetTeiTypeUid: String
)

data class View(
    val teiPrimaryAttribute: String,
    val teiSecondaryAttribute: String,
    val teiTertiaryAttribute: String
)