package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipType

class GetRelationshipsByType(
    private val relationshipsRepository: RelationshipsRepository,
    private val dispatcher: DispatcherProvider,
) {
    suspend operator fun invoke(): List<RelationshipType> = withContext(dispatcher.io()) {
        relationshipsRepository.getRelationshipTypes()
    }
}
