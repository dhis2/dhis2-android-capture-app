package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.tracker.relationships.data.RelationshipsRepositoryActions

/*
 * This use case deletes provided relationships.
 */
class DeleteRelationships(
    private val relationshipsRepository: RelationshipsRepositoryActions,
) {
    suspend operator fun invoke(relationships: List<String>): Flow<Result<Unit>> {
        var result = Result.success(Unit)
        relationships.forEach {
            try {
                relationshipsRepository.deleteRelationship(it)
            } catch (error: Exception) {
                result = Result.failure(error)
            }
        }
        return flowOf(result)
    }
}
