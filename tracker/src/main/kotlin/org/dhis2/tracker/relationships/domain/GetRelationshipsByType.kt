package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationshipItem
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.tracker.ui.AvatarProvider

/*
 * This use case fetches all the relationships that the tei has access to grouped by their type.
 */
class GetRelationshipsByType(
    private val relationshipsRepository: RelationshipsRepository,
    private val dateLabelProvider: DateLabelProvider,
    private val avatarProvider: AvatarProvider,
) {
    operator fun invoke(): Flow<List<RelationshipSection>> =
        relationshipsRepository.getRelationshipTypes()
            .combine(
                relationshipsRepository.getRelationships()
            ) { types, relationships ->
                types.map { type ->
                    val relationshipType = type.first
                    val teiTypeUid = type.second

                    // Filter relationships once based on relationshipType
                    val filteredRelationships = relationships.filter {
                        it.relationshipType.uid() == relationshipType.uid()
                    }

                    RelationshipSection(
                        title = relationshipsRepository.getRelationshipTitle(relationshipType),
                        relationshipType = relationshipType,
                        relationships = filteredRelationships.map { mapToRelationshipItem(it) },
                        teiTypeUid = teiTypeUid
                    )
                }
            }

    private fun mapToRelationshipItem(relationship: RelationshipModel): RelationshipItem {
        return RelationshipItem(
            uid = relationship.relationship.uid() ?: "",
            title = relationship.displayRelationshipName(),
            description = relationship.displayDescription(),
            attributes = relationship.displayAttributes(),
            ownerType = relationship.ownerType,
            ownerUid = relationship.ownerUid,
            avatar = avatarProvider.getAvatar(
                style = relationship.ownerStyle,
                profilePath = relationship.getPicturePath(),
                firstAttributeValue = relationship.firstMainValue(),
            ),
            canOpen = relationship.canBeOpened,
            lastUpdated = dateLabelProvider.span(relationship.displayLastUpdated())
        )
    }
}
