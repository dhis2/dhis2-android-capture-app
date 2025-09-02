package org.dhis2.tracker.relationships.domain

import org.dhis2.tracker.relationships.data.RelationshipsRepositoryActions
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide

class AddRelationship(
    private val repository: RelationshipsRepositoryActions,
) {
    suspend operator fun invoke(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        relationshipSide: RelationshipConstraintSide,
    ): Result<String> =
        repository.addRelationship(
            selectedTeiUid = selectedTeiUid,
            relationshipTypeUid = relationshipTypeUid,
            relationshipSide = relationshipSide,
        )
}
