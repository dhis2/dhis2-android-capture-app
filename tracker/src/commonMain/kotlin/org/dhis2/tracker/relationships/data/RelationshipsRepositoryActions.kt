package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.flow.Flow
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipSection

interface RelationshipsRepositoryActions {
    abstract suspend fun getRelationshipTypes(): List<RelationshipSection>

    abstract suspend fun getRelationshipsGroupedByTypeAndSide(relationshipSection: RelationshipSection): RelationshipSection

    abstract suspend fun getRelationships(): Flow<List<RelationshipModel>>

    suspend fun deleteRelationship(relationshipUid: String)
    suspend fun addRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        relationshipSide: RelationshipConstraintSide,
    ): Result<String>

    fun hasWritePermission(relationshipTypeUid: String): Boolean
}
