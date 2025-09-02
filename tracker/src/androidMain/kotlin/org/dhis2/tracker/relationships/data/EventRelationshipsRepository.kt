package org.dhis2.tracker.relationships.data

import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipGeometry
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipConstraintType
import org.hisp.dhis.android.core.relationship.RelationshipHelper
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemEvent
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class EventRelationshipsRepository(
    private val d2: D2,
    resources: ResourceManager,
    private val eventUid: String,
    private val profilePictureProvider: ProfilePictureProvider,
) : RelationshipsRepository(d2, resources) {
    override suspend fun getRelationshipTypes(): List<RelationshipSection> {
        val event =
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet()
        val programStageUid = event?.programStage() ?: ""

        return d2
            .relationshipModule()
            .relationshipService()
            .getRelationshipTypesForEvents(
                programStageUid = programStageUid,
            ).map { relationshipWithEntitySide ->
                RelationshipSection(
                    uid = relationshipWithEntitySide.relationshipType.uid(),
                    title =
                        getRelationshipTitle(
                            relationshipWithEntitySide.relationshipType,
                            relationshipWithEntitySide.entitySide,
                        ),
                    relationships = emptyList(),
                    side =
                        when (relationshipWithEntitySide.entitySide) {
                            RelationshipConstraintType.TO -> RelationshipConstraintSide.TO
                            RelationshipConstraintType.FROM -> RelationshipConstraintSide.FROM
                        },
                    entityToAdd =
                        when (relationshipWithEntitySide.entitySide) {
                            RelationshipConstraintType.FROM ->
                                relationshipWithEntitySide.relationshipType
                                    .toConstraint()
                                    ?.trackedEntityType()
                                    ?.uid()

                            RelationshipConstraintType.TO ->
                                relationshipWithEntitySide.relationshipType
                                    .fromConstraint()
                                    ?.trackedEntityType()
                                    ?.uid()
                        },
                )
            }
    }

    override suspend fun getRelationshipsGroupedByTypeAndSide(relationshipSection: RelationshipSection): RelationshipSection {
        val constraintType =
            when (relationshipSection.side) {
                RelationshipConstraintSide.FROM -> RelationshipConstraintType.FROM
                RelationshipConstraintSide.TO -> RelationshipConstraintType.TO
            }
        val relationshipType =
            d2
                .relationshipModule()
                .relationshipTypes()
                .withConstraints()
                .uid(relationshipSection.uid)
                .blockingGet()

        val relationships =
            d2
                .relationshipModule()
                .relationships()
                .byItem(
                    RelationshipItem
                        .builder()
                        .event(
                            RelationshipItemEvent.builder().event(eventUid).build(),
                        ).relationshipItemType(constraintType)
                        .build(),
                ).byRelationshipType()
                .eq(relationshipSection.uid)
                .byDeleted()
                .isFalse
                .withItems()
                .blockingGet()
                .mapNotNull { relationship ->
                    mapToRelationshipModel(
                        relationship = relationship,
                        relationshipType = relationshipType,
                        eventUid = eventUid,
                    )
                }
        return relationshipSection.copy(
            relationships = relationships,
        )
    }

    override suspend fun getRelationships(): List<RelationshipModel> =
        d2
            .relationshipModule()
            .relationships()
            .getByItem(
                RelationshipItem
                    .builder()
                    .event(
                        RelationshipItemEvent.builder().event(eventUid).build(),
                    ).build(),
            ).mapNotNull { relationship ->
                getRelationshipTypeByUid(
                    relationship.relationshipType(),
                )?.let { type ->
                    mapToRelationshipModel(
                        relationship = relationship,
                        relationshipType = type,
                        eventUid = eventUid,
                    )
                }
            }

    override fun createRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        relationshipSide: RelationshipConstraintSide,
    ): Relationship {
        val (fromUid, toUid) =
            when (relationshipSide) {
                RelationshipConstraintSide.FROM -> Pair(eventUid, selectedTeiUid)
                RelationshipConstraintSide.TO -> Pair(selectedTeiUid, eventUid)
            }
        return RelationshipHelper.eventToTeiRelationship(
            fromUid,
            toUid,
            relationshipTypeUid,
        )
    }

    private suspend fun mapToRelationshipModel(
        relationship: Relationship,
        relationshipType: RelationshipType?,
        eventUid: String,
    ): RelationshipModel? {
        val relationshipOwnerUid: String?
        val direction: RelationshipDirection
        if (eventUid != relationship.from()?.event()?.event()) {
            relationshipOwnerUid =
                relationship.from()?.trackedEntityInstance()?.trackedEntityInstance()
            direction = RelationshipDirection.FROM
        } else {
            relationshipOwnerUid =
                relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()
            direction = RelationshipDirection.TO
        }
        if (relationshipOwnerUid == null) return null

        val event =
            d2
                .eventModule()
                .events()
                .withTrackedEntityDataValues()
                .uid(eventUid)
                .blockingGet()
        val tei =
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .withTrackedEntityAttributeValues()
                .uid(relationshipOwnerUid)
                .blockingGet()

        val eventDescription =
            event?.programStage()?.let { stage ->
                getStage(stage)?.displayDescription()
            }

        val (fromGeometry, toGeometry) =
            getGeometries(
                direction = direction,
                tei = tei,
                event = event,
            )
        val (fromValues, toValues) =
            getValues(
                direction = direction,
                relationshipOwnerUid = relationshipOwnerUid,
                relationshipType = relationshipType,
                relationship = relationship,
            )
        val (fromProfilePic, toProfilePic) =
            getProfilePics(
                direction = direction,
                tei = tei,
            )
        val (fromDefaultPic, toDefaultPic) =
            getDefaultPics(
                direction = direction,
                tei = tei,
                event = event,
            )

        val canBeOpened =
            canBeOpened(
                direction = direction,
                tei = tei,
                event = event,
            )

        val (fromLastUpdated, toLastUpdated) =
            getLastUpdatedInfo(
                direction = direction,
                tei = tei,
                event = event,
            )

        val (fromDescription, toDescription) =
            getDescriptions(
                direction = direction,
                eventDescription = eventDescription,
            )

        val ownerStyle = getOwnerStyle(relationshipOwnerUid, RelationshipOwnerType.TEI)

        return RelationshipModel(
            relationship.uid() ?: "",
            (relationship.syncState() ?: State.SYNCED).name,
            fromGeometry?.let { geometry ->
                RelationshipGeometry(
                    geometry.type()?.name,
                    geometry.coordinates(),
                )
            },
            toGeometry?.let { geometry ->
                RelationshipGeometry(
                    geometry.type()?.name,
                    geometry.coordinates(),
                )
            },
            direction,
            relationshipOwnerUid,
            RelationshipOwnerType.TEI,
            fromValues,
            toValues,
            fromProfilePic,
            toProfilePic,
            fromDefaultPic,
            toDefaultPic,
            ownerStyle?.icon(),
            ownerStyle?.color(),
            canBeOpened,
            toLastUpdated,
            fromLastUpdated,
            toDescription,
            fromDescription,
        )
    }

    private fun canBeOpened(
        direction: RelationshipDirection,
        tei: TrackedEntityInstance?,
        event: Event?,
    ) = if (direction == RelationshipDirection.FROM) {
        tei?.syncState() != State.RELATIONSHIP &&
            orgUnitInScope(tei?.organisationUnit())
    } else {
        event?.syncState() != State.RELATIONSHIP &&
            orgUnitInScope(event?.organisationUnit())
    }

    private fun getDescriptions(
        direction: RelationshipDirection,
        eventDescription: String?,
    ) = if (direction == RelationshipDirection.FROM) {
        Pair(
            null,
            eventDescription,
        )
    } else {
        Pair(
            eventDescription,
            null,
        )
    }

    private fun getLastUpdatedInfo(
        direction: RelationshipDirection,
        tei: TrackedEntityInstance?,
        event: Event?,
    ) = if (direction == RelationshipDirection.FROM) {
        Pair(
            tei?.lastUpdated(),
            event?.lastUpdated(),
        )
    } else {
        Pair(
            event?.lastUpdated(),
            tei?.lastUpdated(),
        )
    }

    private fun getDefaultPics(
        direction: RelationshipDirection,
        tei: TrackedEntityInstance?,
        event: Event?,
    ) = if (direction == RelationshipDirection.FROM) {
        Pair(
            getTeiDefaultRes(tei),
            getEventDefaultRes(event),
        )
    } else {
        Pair(
            getEventDefaultRes(event),
            getTeiDefaultRes(tei),
        )
    }

    private fun getGeometries(
        direction: RelationshipDirection,
        tei: TrackedEntityInstance?,
        event: Event?,
    ) = if (direction == RelationshipDirection.FROM) {
        Pair(
            tei?.geometry(),
            event?.geometry(),
        )
    } else {
        Pair(
            event?.geometry(),
            tei?.geometry(),
        )
    }

    private fun getProfilePics(
        direction: RelationshipDirection,
        tei: TrackedEntityInstance?,
    ) = if (direction == RelationshipDirection.FROM) {
        Pair(
            tei?.let { profilePictureProvider(it, null) },
            null,
        )
    } else {
        Pair(
            null,
            tei?.let { profilePictureProvider(it, null) },
        )
    }

    private suspend fun getValues(
        direction: RelationshipDirection,
        relationshipOwnerUid: String?,
        relationshipType: RelationshipType?,
        relationship: Relationship,
    ) = if (direction == RelationshipDirection.FROM) {
        Pair(
            getTeiAttributesForRelationship(
                relationshipOwnerUid,
                relationshipType?.fromConstraint(),
                relationship.created(),
            ),
            getEventValuesForRelationship(
                eventUid,
                relationshipType?.toConstraint(),
                relationship.created(),
            ),
        )
    } else {
        Pair(
            getEventValuesForRelationship(
                eventUid,
                relationshipType?.fromConstraint(),
                relationship.created(),
            ),
            getTeiAttributesForRelationship(
                relationshipOwnerUid,
                relationshipType?.toConstraint(),
                relationship.created(),
            ),
        )
    }
}
