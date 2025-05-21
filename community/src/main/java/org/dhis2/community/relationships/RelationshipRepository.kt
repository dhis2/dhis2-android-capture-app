package org.dhis2.community.relationships

import com.google.gson.Gson
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import timber.log.Timber

class RelationshipRepository(
    private val d2: D2
) {

    fun getRelationshipConfig(): RelationshipConfig {
        val entries = d2.dataStoreModule()
            .dataStore()
            .byNamespace()
            .eq("community_redesign")
            .blockingGet()

        return entries.firstOrNull { it.key() == "relationships" }
            ?.let { Gson().fromJson(it.value(), RelationshipConfig::class.java) }
            ?: RelationshipConfig(emptyList())
    }

    fun createAndAddRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        teiUid: String,
        relationshipSide: RelationshipConstraintSide
    ): Result<String> {
        return try {
            val (fromUid, toUid) = when (relationshipSide) {
                RelationshipConstraintSide.FROM -> Pair(teiUid, selectedTeiUid)
                RelationshipConstraintSide.TO -> Pair(selectedTeiUid, teiUid)
            }

            val relationship = RelationshipHelper.teiToTeiRelationship(
                fromUid, toUid, relationshipTypeUid
            )

            val relationshipUid = d2.relationshipModule().relationships().blockingAdd(relationship)
            Result.success(relationshipUid)
        } catch (error: Exception) {
            Timber.e(error)
            Result.failure(error)
        }
    }

    fun deleteRelationship(relationshipUid: String): Result<Unit> {
        return try {
            d2.relationshipModule()
                .relationships()
                .uid(relationshipUid)
                .blockingDelete()
            Result.success(Unit)
        } catch (error: Exception) {
            Timber.e(error)
            Result.failure(error)
        }
    }
}