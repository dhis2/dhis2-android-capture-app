package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipViewModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemEvent
import org.hisp.dhis.android.core.relationship.RelationshipType

class EventRelationshipsRepository(
    private val d2: D2,
    resources: ResourceManager,
    private val eventUid: String,
    private val profilePictureProvider: ProfilePictureProvider,
) : RelationshipsRepository(d2, resources) {

    override fun getRelationshipTypes(): Flow<List<Pair<RelationshipType, String?>>> {
        val event = d2.eventModule().events().uid(eventUid).blockingGet()
        val programStageUid = event?.programStage() ?: ""
        val programUid = event?.program() ?: ""
        return flowOf(d2.relationshipModule()
            .relationshipTypes()
            .withConstraints()
            .byAvailableForEvent(event?.uid() ?: "")
            .blockingGet().mapNotNull { relationshipType ->
                val secondaryUid = when {
                    relationshipType.fromConstraint()?.programStage()
                        ?.uid() == programStageUid ->
                        relationshipType.toConstraint()?.trackedEntityType()?.uid()

                    relationshipType.fromConstraint()?.program()?.uid() == programUid ->
                        relationshipType.toConstraint()?.trackedEntityType()?.uid()

                    relationshipType.bidirectional() == true && relationshipType.toConstraint()
                        ?.programStage()?.uid() == programStageUid ->
                        relationshipType.fromConstraint()?.trackedEntityType()?.uid()

                    relationshipType.bidirectional() == true && relationshipType.toConstraint()
                        ?.program()?.uid() == programUid ->
                        relationshipType.fromConstraint()?.trackedEntityType()?.uid()

                    else -> null
                }
                secondaryUid?.let {
                    Pair(relationshipType, secondaryUid)
                }
            }
        )
    }

    override fun getRelationships(): Flow<List<RelationshipViewModel>> {
        return flowOf(
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().event(
                    RelationshipItemEvent.builder().event(eventUid).build(),
                ).build(),
            ).mapNotNull { relationship ->
                val relationshipType =
                    d2.relationshipModule().relationshipTypes().withConstraints()
                        .uid(relationship.relationshipType())
                        .blockingGet() ?: return@mapNotNull null

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
                if (relationshipOwnerUid == null) return@mapNotNull null

                val event = d2.eventModule().events()
                    .withTrackedEntityDataValues().uid(eventUid).blockingGet()
                val tei = d2.trackedEntityModule().trackedEntityInstances()
                    .withTrackedEntityAttributeValues().uid(relationshipOwnerUid).blockingGet()

                val eventDescription = event?.programStage()?.let { stage ->
                    getStage(stage)?.displayDescription()
                }

                val (fromGeometry, toGeometry) =
                    if (direction == RelationshipDirection.FROM) {
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
                val (fromValues, toValues) =
                    if (direction == RelationshipDirection.FROM) {
                        Pair(
                            getTeiAttributesForRelationship(
                                relationshipOwnerUid,
                                relationshipType.fromConstraint(),
                                relationship.created(),
                            ),
                            getEventValuesForRelationship(
                                eventUid,
                                relationshipType.toConstraint(),
                                relationship.created(),
                            ),
                        )
                    } else {
                        Pair(
                            getEventValuesForRelationship(
                                eventUid,
                                relationshipType.fromConstraint(),
                                relationship.created(),
                            ),
                            getTeiAttributesForRelationship(
                                relationshipOwnerUid,
                                relationshipType.toConstraint(),
                                relationship.created(),
                            ),
                        )
                    }

                val (fromProfilePic, toProfilePic) =
                    if (direction == RelationshipDirection.FROM) {
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

                val (fromDefaultPic, toDefaultPic) =
                    if (direction == RelationshipDirection.FROM) {
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

                val canBeOpened = if (direction == RelationshipDirection.FROM) {
                    tei?.syncState() != State.RELATIONSHIP &&
                            orgUnitInScope(tei?.organisationUnit())
                } else {
                    event?.syncState() != State.RELATIONSHIP &&
                            orgUnitInScope(event?.organisationUnit())
                }

                val (fromLastUpdated, toLastUpdated) =
                    if (direction == RelationshipDirection.FROM) {
                        Pair(
                            tei?.lastUpdated(),
                            event?.lastUpdated()
                        )
                    } else {
                        Pair(
                            event?.lastUpdated(),
                            tei?.lastUpdated()
                        )
                    }

                val (fromDescription, toDescription) =
                    if (direction == RelationshipDirection.FROM) {
                        Pair(
                            null,
                            eventDescription
                        )
                    } else {
                        Pair(
                            eventDescription,
                            null
                        )
                    }

                RelationshipViewModel(
                    relationship,
                    fromGeometry,
                    toGeometry,
                    relationshipType,
                    direction,
                    relationshipOwnerUid,
                    RelationshipOwnerType.TEI,
                    fromValues,
                    toValues,
                    fromProfilePic,
                    toProfilePic,
                    fromDefaultPic,
                    toDefaultPic,
                    getOwnerStyle(relationshipOwnerUid, RelationshipOwnerType.TEI),
                    canBeOpened,
                    toLastUpdated,
                    fromLastUpdated,
                    toDescription,
                    fromDescription,
                )
            }
        )
    }
}
