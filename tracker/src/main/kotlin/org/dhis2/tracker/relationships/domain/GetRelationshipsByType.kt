package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.hisp.dhis.android.core.D2

/*
 * This use case fetches all the relationships that the tei has access to grouped by their type.
 */
class GetRelationshipsByType(
    d2: D2,
    private val dispatcher: DispatcherProvider,
) {
    suspend operator fun invoke(teiUid: String): List<RelationshipSection> =
        withContext(dispatcher.io()) {
            listOf(
                RelationshipSection(
                    title = "Parents",
                    description = "No data",
                ),
                RelationshipSection(
                    title = "Sisters",
                    description = "No data",
                ),
                RelationshipSection(
                    title = "Brothers",
                    description = "No data",
                ),
            )
        }


}