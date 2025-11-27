package org.dhis2.tracker.relationships.ui.state

import org.dhis2.tracker.relationships.model.RelationshipConstraintSide

data class RelationshipSectionUiState(
    val uid: String,
    val title: String,
    val relationships: List<RelationshipItemUiState>,
    val side: RelationshipConstraintSide,
    val entityToAdd: String?,
)
