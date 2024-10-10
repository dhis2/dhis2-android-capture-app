package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
                types.map { type ->
                    val relationshipType = type.first
                    val teiTypeUid = type.second
                    RelationshipSection(
                        relationshipType = relationshipType,
                        relationships = relationships.filter {
                            it.relationshipType.uid() == type.first.uid()
                        }.map {
                            // We have RelationshipViewModel and we want to map it to RelationShipItem

                            RelationShipItem(
                                title = it.displayRelationshipName(),
                                description = it.displayDescription(),
                                attributes = it.displayAttributes(),
                                ownerType = it.ownerType,
                                ownerUid = it.ownerUid,
                                avatar = getAvatar(
                                    style = relationshipsRepository.getProgramStyle(),
                                    profilePath = it.getPicturePath(),
                                    firstAttributeValue = it.firstMainValue(),
                                ),
                                canOpen = it.canBeOpened,
                                lastUpdated = dateLabelProvider.span(it.displayLastUpdated()),
                            )
                        },
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
}
