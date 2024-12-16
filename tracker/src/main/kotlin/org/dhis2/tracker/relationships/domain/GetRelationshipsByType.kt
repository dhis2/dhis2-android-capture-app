package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend operator fun invoke(): List<RelationshipSection> = withContext(Dispatchers.IO) {
        relationshipsRepository.getRelationshipTypes().map { relationshipType ->

            RelationshipSection(
                uid = relationshipType.uid,
                title = relationshipType.title,
                relationships = relationshipType.relationships.map { mapToRelationshipItem(it) },
                side = relationshipType.side,
                entityToAdd = relationshipType.entityToAdd,
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
