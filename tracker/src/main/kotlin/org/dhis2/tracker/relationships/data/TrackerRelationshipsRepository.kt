package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.relationship.RelationshipType
import java.util.Date

class TrackerRelationshipsRepository(
    private val d2: D2,
    resources: ResourceManager,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val profilePictureProvider: ProfilePictureProvider,
) : RelationshipsRepository(d2, resources) {

    override fun getRelationshipTypes(): Flow<List<Pair<RelationshipType, String?>>> {
        val teTypeUid = d2.trackedEntityModule().trackedEntityInstances()
            .uid(teiUid)
            .blockingGet()?.trackedEntityType() ?: return flowOf(emptyList())

        return flowOf(d2.relationshipModule()
            .relationshipTypes()
            .withConstraints()
            .byAvailableForTrackedEntityInstance(teiUid)
            .blockingGet().map { relationshipType ->
                val secondaryTeTypeUid = when {
                    relationshipType.fromConstraint()?.trackedEntityType()
                        ?.uid() == teTypeUid ->
                        relationshipType.toConstraint()?.trackedEntityType()?.uid()

                    relationshipType.bidirectional() == true && relationshipType.toConstraint()
                        ?.trackedEntityType()?.uid() == teTypeUid ->
                        relationshipType.fromConstraint()?.trackedEntityType()?.uid()

                    else -> null
                }
                Pair(relationshipType, secondaryTeTypeUid)
            }
        )
    }

    override fun getRelationships(): Flow<List<RelationshipModel>> {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(teiUid).blockingGet()
        val programUid = d2.enrollmentModule().enrollments()
            .uid(enrollmentUid).blockingGet()?.program()

        return flowOf(
            //Gets all the relationships by the tei uid
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().trackedEntityInstance(
                    RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teiUid)
                        .build(),
                ).build(),
            ).mapNotNull { relationship ->
                //maps each relationship to a model

                //Gets the relationship type
                val relationshipType = getRelationshipTypeByUid(
                    relationship.relationshipType()
                ) ?: return@mapNotNull null
                val direction: RelationshipDirection
                val relationshipOwnerUid: String?
                val relationshipOwnerType: RelationshipOwnerType?
                val fromGeometry: Geometry?
                val toGeometry: Geometry?
                val fromValues: List<Pair<String, String>>
                val toValues: List<Pair<String, String>>
                val fromProfilePic: String?
                val toProfilePic: String?
                val fromDefaultPicRes: Int
                val toDefaultPicRes: Int
                val canBoOpened: Boolean
                val toLastUpdated: Date?
                val fromLastUpdated: Date?
                val toDescription: String?
                val fromDescription: String?

                //Here checks if the TEI is the from or to of the relationship
                when (teiUid) {
                    relationship.from()?.trackedEntityInstance()?.trackedEntityInstance() -> {
                        direction = RelationshipDirection.TO
                        fromGeometry = tei?.geometry()
                        fromValues = getTeiAttributesForRelationship(
                            teiUid,
                            relationshipType.fromConstraint(),
                            relationship.created()
                        )
                        fromProfilePic = tei?.let { profilePictureProvider(it, programUid) }
                        fromDefaultPicRes = getTeiDefaultRes(tei)
                        fromLastUpdated = tei?.lastUpdated()
                        fromDescription = null
                        // If the relationship is to a TEI then the owner is the TEI
                        if (relationship.to()?.trackedEntityInstance() != null) {
                            relationshipOwnerType = RelationshipOwnerType.TEI
                            relationshipOwnerUid =
                                relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()
                            val toTei = d2.trackedEntityModule().trackedEntityInstances()
                                .uid(relationshipOwnerUid).blockingGet()
                            toGeometry = toTei?.geometry()
                            toValues = getTeiAttributesForRelationship(
                                toTei?.uid(),
                                relationshipType.toConstraint(),
                                relationship.created()
                            )
                            toProfilePic = toTei?.let { profilePictureProvider(it, programUid) }
                            toDefaultPicRes = getTeiDefaultRes(toTei)
                            canBoOpened = toTei?.syncState() != State.RELATIONSHIP &&
                                    orgUnitInScope(toTei?.organisationUnit())
                            toLastUpdated = toTei?.lastUpdated()
                            toDescription = null
                        } else {
                            // If the relationship is not to a TEI then the owner is the event
                            relationshipOwnerType = RelationshipOwnerType.EVENT
                            relationshipOwnerUid =
                                relationship.to()?.event()?.event()
                            val toEvent = d2.eventModule().events()
                                .uid(relationshipOwnerUid).blockingGet()
                            toGeometry = toEvent?.geometry()
                            toValues = getEventValuesForRelationship(
                                toEvent?.uid(),
                                relationshipType.toConstraint(),
                                relationship.created(),
                            )
                            toProfilePic = ""
                            toDefaultPicRes = getEventDefaultRes(toEvent)
                            canBoOpened = toEvent?.syncState() != State.RELATIONSHIP &&
                                    orgUnitInScope(toEvent?.organisationUnit())
                            toLastUpdated = toEvent?.lastUpdated()
                            toDescription = toEvent?.programStage()?.let { stage ->
                                getStage(stage)?.displayDescription()
                            }
                        }
                    }

                    relationship.to()?.trackedEntityInstance()?.trackedEntityInstance() -> {
                        direction = RelationshipDirection.FROM
                        toGeometry = tei?.geometry()
                        toValues = getTeiAttributesForRelationship(
                            teiUid,
                            relationshipType.toConstraint(),
                            relationship.created(),
                        )
                        toProfilePic = tei?.let { profilePictureProvider(it, programUid) }
                        toDefaultPicRes = getTeiDefaultRes(tei)
                        toLastUpdated = tei?.lastUpdated()
                        toDescription = null
                        if (relationship.from()?.trackedEntityInstance() != null) {
                            relationshipOwnerType = RelationshipOwnerType.TEI
                            relationshipOwnerUid =
                                relationship.from()?.trackedEntityInstance()
                                    ?.trackedEntityInstance()
                            val fromTei = d2.trackedEntityModule().trackedEntityInstances()
                                .uid(relationshipOwnerUid).blockingGet()
                            fromGeometry = fromTei?.geometry()
                            fromValues = getTeiAttributesForRelationship(
                                fromTei?.uid(),
                                relationshipType.fromConstraint(),
                                relationship.created(),
                            )
                            fromProfilePic = fromTei?.let { profilePictureProvider(it, programUid) }
                            fromDefaultPicRes = getTeiDefaultRes(fromTei)
                            canBoOpened = fromTei?.syncState() != State.RELATIONSHIP &&
                                    orgUnitInScope(fromTei?.organisationUnit())
                            fromLastUpdated = fromTei?.lastUpdated()
                            fromDescription = null
                        } else {
                            relationshipOwnerType = RelationshipOwnerType.EVENT
                            relationshipOwnerUid =
                                relationship.from()?.event()?.event()
                            val fromEvent = d2.eventModule().events()
                                .uid(relationshipOwnerUid).blockingGet()
                            fromGeometry = fromEvent?.geometry()
                            fromValues = getEventValuesForRelationship(
                                fromEvent?.uid(),
                                relationshipType.fromConstraint(),
                                relationship.created(),
                            )
                            fromProfilePic = ""
                            fromDefaultPicRes = getEventDefaultRes(fromEvent)
                            canBoOpened = fromEvent?.syncState() != State.RELATIONSHIP &&
                                    orgUnitInScope(fromEvent?.organisationUnit())
                            fromLastUpdated = fromEvent?.lastUpdated()
                            fromDescription = fromEvent?.programStage()?.let { stage ->
                                getStage(stage)?.displayDescription()
                            }
                        }
                    }

                    else -> return@mapNotNull null
                }

                if (relationshipOwnerUid == null) return@mapNotNull null

                RelationshipModel(
                    relationship,
                    fromGeometry,
                    toGeometry,
                    relationshipType,
                    direction,
                    relationshipOwnerUid,
                    relationshipOwnerType,
                    fromValues,
                    toValues,
                    fromProfilePic,
                    toProfilePic,
                    fromDefaultPicRes,
                    toDefaultPicRes,
                    getOwnerStyle(relationshipOwnerUid, relationshipOwnerType),
                    canBoOpened,
                    toLastUpdated,
                    fromLastUpdated,
                    toDescription,
                    fromDescription,
                )
            }
        )
    }

    override fun getRelationshipTitle(relationshipType: RelationshipType): String {
        val teTypeUid = d2.trackedEntityModule().trackedEntityInstances()
            .uid(teiUid)
            .blockingGet()?.trackedEntityType()
        return when (teTypeUid) {
            relationshipType.fromConstraint()?.trackedEntityType()?.uid() -> {
                relationshipType.fromToName() ?: relationshipType.displayName() ?: ""
            }

            relationshipType.toConstraint()?.trackedEntityType()?.uid() -> {
                relationshipType.toFromName() ?: relationshipType.displayName() ?: ""
            }

            else -> {
                relationshipType.displayName() ?: ""
            }
        }
    }

    private fun getRelationshipTypeByUid(relationshipTypeUid: String?) =
        d2.relationshipModule().relationshipTypes().withConstraints()
            .uid(relationshipTypeUid)
            .blockingGet()
}
