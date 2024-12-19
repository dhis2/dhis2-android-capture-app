package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipSection

class GetRelationshipsByType(
    private val relationshipsRepository: RelationshipsRepository,
    private val dispatcher: DispatcherProvider,
) {
    suspend operator fun invoke(): List<RelationshipSection> = withContext(dispatcher.io()) {
        val relationshipSections = relationshipsRepository.getRelationshipTypes()
        relationshipSections.map { relationshipSection ->
            relationshipsRepository.getRelationshipsGroupedByTypeAndSide(relationshipSection)
        }
    }
}
