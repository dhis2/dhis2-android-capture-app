package org.dhis2.tracker.relationships.domain

import org.dhis2.tracker.relationships.data.RelationshipsRepository

/*
 * This use case deletes provided relationships.
 */
class DeleteRelationships(
    private val relationshipsRepository: RelationshipsRepository,
) {
    operator fun invoke(relationships: List<String>) {
        relationships.forEach {
            relationshipsRepository.deleteRelationship(it)
        }
    }
}
