package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipDirection

class AddRelationship(
    private val dispatcher: DispatcherProvider,
    private val repository: RelationshipsRepository,
) {
    suspend operator fun invoke(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        direction: RelationshipDirection,
    ): Result<String> = withContext(dispatcher.io()) {
        val relationship = repository.createRelationship(
            selectedTeiUid = selectedTeiUid,
            relationshipTypeUid = relationshipTypeUid,
            direction = direction,
        )
        repository.addRelationship(relationship)
    }
}
