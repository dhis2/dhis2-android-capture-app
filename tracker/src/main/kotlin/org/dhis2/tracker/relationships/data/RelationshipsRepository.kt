package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.data.RelationshipViewModel
import org.hisp.dhis.android.core.relationship.RelationshipType

interface RelationshipsRepository {
    fun getRelationshipTypes(): Flow<List<Pair<RelationshipType, String>>>
    fun getRelationships(): Flow<List<RelationshipViewModel>>
}