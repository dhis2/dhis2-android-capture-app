package org.dhis2.tracker.relationships.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.dhis2.commons.data.RelationshipDirection
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.tracker.relationships.data.RelationshipsRepository
import org.dhis2.tracker.relationships.model.RelationShipItem
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.dhis2.ui.avatar.AvatarProviderConfiguration

/*
 * This use case fetches all the relationships that the tei has access to grouped by their type.
 */
class GetRelationshipsByType(
    private val teiUid: String,
    private val enrollmentUid: String,
    private val relationShipRepository: RelationshipsRepository,
    private val dateLabelProvider: DateLabelProvider,
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
                        relationships = relationships.filter {
                            it.relationshipType.uid() == type.first.uid()
                        }.map {
                            // here we have RelationshipViewModel and we want to map it to RelationShipItem
                            val (attributes, profilePath, lastUpdated) = when (it.direction) {
                                RelationshipDirection.FROM -> Triple(
                                    it.fromValues,
                                    it.fromImage,
                                    it.fromLastUpdated,
                                )

                                RelationshipDirection.TO -> Triple(
                                    it.toValues,
                                    it.toImage,
                                    it.toLastUpdated,
                                )
                            }

                            RelationShipItem(
                                title = it.displayRelationshipName(),
                                attributes = attributes,
                                ownerType = it.ownerType,
                                ownerUid = it.ownerUid,
                                avatar = AvatarProviderConfiguration.ProfilePic(
                                    profilePicturePath = profilePath ?: "",
                                    firstMainValue = attributes.firstOrNull()?.second?.firstOrNull()
                                        ?.toString() ?: "",
                                ),
                                canOpen = it.canBeOpened,
                                lastUpdated = dateLabelProvider.span(lastUpdated),
                            )
                        },
                        teiTypeUid = teiTypeUid
                    )
                }
            }
}
