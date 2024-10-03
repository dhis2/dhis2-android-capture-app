package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipSection

/*
 * This use case fetches all the relationships that the tei has access to grouped by their type.
 */
class GetRelationshipsByType(
    private val teiUid: String,
    private val enrollmentUid: String,
    private val relationShipRepository: RelationshipsRepository,
) {
    operator fun invoke(): Flow<List<RelationshipSection>> =
        relationShipRepository.getRelationshipTypes(teiUid)
            .combine(
                relationShipRepository.getRelationships(
                    teiUid,
                    enrollmentUid
                )
            ) { types, relationships ->
                types.map { type ->
                    val relationshipType = type.first
                    val teiTypeUid = type.second
                    RelationshipSection(
                        relationshipType = relationshipType,
                        relationships = relationships.filter { it.relationshipType.uid() == type.first.uid() },
                        teiTypeUid = teiTypeUid
                    )
                }
            }
}