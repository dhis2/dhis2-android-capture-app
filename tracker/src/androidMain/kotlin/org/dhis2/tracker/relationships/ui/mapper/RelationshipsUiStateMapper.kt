package org.dhis2.tracker.relationships.ui.mapper

import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.tracker.relationships.ui.state.RelationshipItemUiState
import org.dhis2.tracker.relationships.ui.state.RelationshipSectionUiState
import org.dhis2.tracker.ui.AvatarProvider

class RelationshipsUiStateMapper(
    private val avatarProvider: AvatarProvider,
    private val dateLabelProvider: DateLabelProvider,
) {
    fun map(relationships: List<RelationshipSection>): List<RelationshipSectionUiState> =
        relationships.map { relationshipType ->
            RelationshipSectionUiState(
                uid = relationshipType.uid,
                title = relationshipType.title,
                relationships =
                    relationshipType.relationships.map {
                        mapToRelationshipItem(it)
                    },
                side = relationshipType.side,
                entityToAdd = relationshipType.entityToAdd,
            )
        }

    private fun mapToRelationshipItem(relationship: RelationshipModel): RelationshipItemUiState =
        RelationshipItemUiState(
            uid = relationship.relationshipUid,
            title = relationship.displayRelationshipName(),
            description = relationship.displayDescription(),
            attributes = relationship.displayAttributes(),
            ownerType = relationship.ownerType,
            ownerUid = relationship.ownerUid,
            avatar =
                avatarProvider.getAvatar(
                    icon = relationship.ownerStyleIcon,
                    color = relationship.ownerStyleColor,
                    profilePath = relationship.getPicturePath(),
                    firstAttributeValue = relationship.firstMainValue(),
                ),
            canOpen = relationship.canBeOpened,
            lastUpdated = dateLabelProvider.span(relationship.displayLastUpdated()),
        )
}
