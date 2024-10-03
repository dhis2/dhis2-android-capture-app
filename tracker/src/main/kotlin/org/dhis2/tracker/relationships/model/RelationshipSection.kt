package org.dhis2.tracker.relationships.model

import org.dhis2.commons.data.RelationshipViewModel
import org.hisp.dhis.android.core.relationship.RelationshipType

data class RelationshipSection(
    val relationships: List<RelationshipViewModel> = emptyList(),
    val teiTypeUid: String,
    val relationshipType: RelationshipType,
)
