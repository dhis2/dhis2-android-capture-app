package org.dhis2.tracker.relationships.domain

import org.dhis2.tracker.relationships.data.RelationshipsRepositoryActions

/*
 * This use case deletes provided relationships.
 */
class DeleteRelationships(
    private val relationshipsRepository: RelationshipsRepositoryActions,
) {
    suspend operator fun invoke(relationships: List<String>): Result<Unit> {
        var result = Result.success(Unit)
        relationships.forEach {
            try {
                relationshipsRepository.deleteRelationship(it)
            } catch (error: Exception) {
                result = Result.failure(error)
            }
        }
        return result
    }
}
