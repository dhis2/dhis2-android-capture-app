package org.dhis2.tracker.relationships.domain

import org.dhis2.tracker.relationships.data.RelationshipsRepositoryActions
import org.dhis2.tracker.relationships.model.RelationshipSection

class GetRelationshipsByType(
    private val relationshipsRepository: RelationshipsRepositoryActions,
) {
    suspend operator fun invoke(): List<RelationshipSection> {
        val relationshipSections = relationshipsRepository.getRelationshipTypes()
        return relationshipSections.map { relationshipSection ->
            relationshipsRepository.getRelationshipsGroupedByTypeAndSide(relationshipSection)
        }
    }
}
