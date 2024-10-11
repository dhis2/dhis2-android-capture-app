package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationShipItem
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.dhis2.ui.avatar.AvatarProviderConfiguration.Metadata
import org.dhis2.ui.avatar.AvatarProviderConfiguration.ProfilePic
import org.hisp.dhis.android.core.common.ObjectStyle

/*
 * This use case fetches all the relationships that the tei has access to grouped by their type.
 */
class GetRelationshipsByType(
    private val relationshipsRepository: RelationshipsRepository,
    private val dateLabelProvider: DateLabelProvider,
    private val metadataIconProvider: MetadataIconProvider,
) {
    operator fun invoke(): Flow<List<RelationshipSection>> =
        relationshipsRepository.getRelationshipTypes()
            .combine(
                relationshipsRepository.getRelationships()
            ) { types, relationships ->
                types.mapNotNull { type ->
                    val relationshipType = type.first
                    val teiTypeUid = type.second

                    // Filter relationships once based on relationshipType
                    val filteredRelationships = relationships.filter {
                        it.relationshipType.uid() == relationshipType.uid()
                    }

                    // Return null if no matching relationships
                    if (filteredRelationships.isEmpty() && teiTypeUid == null) {
                        return@mapNotNull null
                    }

                    RelationshipSection(
                        relationshipType = relationshipType,
                        relationships = filteredRelationships.map { mapToRelationshipItem(it) },
                        teiTypeUid = teiTypeUid
                    )
                }
            }

    private fun getAvatar(
        style: ObjectStyle? = null,
        profilePath: String,
        firstAttributeValue: String?,
    ): AvatarProviderConfiguration {

        return when {
            profilePath.isNotEmpty() -> {
                ProfilePic(
                    profilePicturePath = profilePath,
                )
            }

            style != null && profilePath.isEmpty() -> {
                Metadata(
                    metadataIconData = metadataIconProvider.invoke(
                        style,
                    ),
                )
            }

            else -> {
                AvatarProviderConfiguration.MainValueLabel(
                    firstMainValue = firstAttributeValue ?: "",
                )
            }
        }
    }

    private fun mapToRelationshipItem(relationship: RelationshipViewModel): RelationShipItem {
        return RelationShipItem(
            title = relationship.displayRelationshipName(),
            description = relationship.displayDescription(),
            attributes = relationship.displayAttributes(),
            ownerType = relationship.ownerType,
            ownerUid = relationship.ownerUid,
            avatar = getAvatar(
                style = relationship.ownerStyle,
                profilePath = relationship.getPicturePath(),
                firstAttributeValue = relationship.firstMainValue(),
            ),
            canOpen = relationship.canBeOpened,
            lastUpdated = dateLabelProvider.span(relationship.displayLastUpdated())
        )
    }
}
