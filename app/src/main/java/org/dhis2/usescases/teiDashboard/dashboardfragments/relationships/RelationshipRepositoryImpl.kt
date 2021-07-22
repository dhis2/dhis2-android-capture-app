package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.Single
import org.dhis2.Bindings.profilePicturePath
import org.dhis2.R
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.relationship.RelationshipEntityType
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemEvent
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.util.ArrayList

class RelationshipRepositoryImpl(
    private val d2: D2,
    private val config: RelationshipConfiguration,
    private val resources: ResourceManager
) : RelationshipRepository {

    override fun relationshipTypes(): Single<List<Pair<RelationshipType, String>>> {
        return when (config) {
            is EventRelationshipConfiguration -> stageRelationshipTypes()
            is TrackerRelationshipConfiguration -> trackerRelationshipTypes()
        }
    }

    override fun relationships(): Single<List<RelationshipViewModel>> {
        return when (config) {
            is EventRelationshipConfiguration -> eventRelationships()
            is TrackerRelationshipConfiguration -> enrollmentRelationships()
        }
    }

    private fun trackerRelationshipTypes(): Single<List<Pair<RelationshipType, String>>> {
        //TODO: Limit link to only TEI
        val teTypeUid = d2.trackedEntityModule().trackedEntityInstances()
            .uid((config as TrackerRelationshipConfiguration).teiUid)
            .blockingGet().trackedEntityType() ?: return Single.just(emptyList())

        return d2.relationshipModule().relationshipTypes()
            .withConstraints()
            .byConstraint(RelationshipEntityType.TRACKED_ENTITY_INSTANCE, teTypeUid)
            .get().map { relationshipTypes ->
                relationshipTypes.mapNotNull { relationshipType ->
                    val secondaryTeTypeUid = when {
                        relationshipType.fromConstraint()?.trackedEntityType()
                            ?.uid() == teTypeUid ->
                            relationshipType.toConstraint()?.trackedEntityType()?.uid()
                        relationshipType.bidirectional() == true && relationshipType.toConstraint()
                            ?.trackedEntityType()?.uid() == teTypeUid ->
                            relationshipType.fromConstraint()?.trackedEntityType()?.uid()
                        else -> null
                    }
                    secondaryTeTypeUid?.let { Pair(relationshipType, secondaryTeTypeUid) }
                }
            }

    }

    private fun stageRelationshipTypes(): Single<List<Pair<RelationshipType, String>>> {
        //TODO: Limit links to TEI
        val programStageUid = (config as EventRelationshipConfiguration).stageUid
        return d2.relationshipModule().relationshipTypes()
            .withConstraints()
            .byConstraint(RelationshipEntityType.PROGRAM_STAGE_INSTANCE, programStageUid)
            .get().map { relationshipTypes ->
                relationshipTypes.mapNotNull { relationshipType ->
                    val secondaryUid = when {
                        relationshipType.fromConstraint()?.programStage()
                            ?.uid() == programStageUid ->
                            relationshipType.toConstraint()?.programStage()?.uid()
                        relationshipType.bidirectional() == true && relationshipType.toConstraint()
                            ?.programStage()?.uid() == programStageUid ->
                            relationshipType.fromConstraint()?.programStage()?.uid()
                        else -> null
                    }
                    secondaryUid?.let { Pair(relationshipType, secondaryUid) }
                }
            }

    }

    fun eventRelationships(): Single<List<RelationshipViewModel>> {
        val eventUid = (config as EventRelationshipConfiguration).eventUid
        return Single.fromCallable {
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().event(
                    RelationshipItemEvent.builder().event(eventUid).build()
                ).build()
            ).map {
                RelationshipViewModel.create(

                )
            }
        }
    }

    fun enrollmentRelationships(): Single<List<RelationshipViewModel>> {
        val teiUid = (config as TrackerRelationshipConfiguration).teiUid
        val programUid =
            d2.enrollmentModule().enrollments().uid(config.enrollmentUid).blockingGet().program()
        return Single.fromCallable {
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().trackedEntityInstance(
                    RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teiUid)
                        .build()
                ).build()
            ).map { relationship ->
                val relationshipType =
                    d2.relationshipModule().relationshipTypes().uid(relationship.relationshipType())
                        .blockingGet()

                val relationshipTEIUid: String?
                val direction: RelationshipViewModel.RelationshipDirection
                if (teiUid != relationship.from()?.trackedEntityInstance()
                        ?.trackedEntityInstance()
                ) {
                    relationshipTEIUid =
                        relationship.from()?.trackedEntityInstance()?.trackedEntityInstance()
                    direction = RelationshipViewModel.RelationshipDirection.FROM
                } else {
                    relationshipTEIUid =
                        relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()
                    direction = RelationshipViewModel.RelationshipDirection.TO
                }

                val fromTeiUid =
                    relationship.from()?.trackedEntityInstance()?.trackedEntityInstance()
                val toTeiUid =
                    relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()

                val fromTei = d2.trackedEntityModule().trackedEntityInstances()
                    .withTrackedEntityAttributeValues().uid(fromTeiUid).blockingGet()
                val toTei = d2.trackedEntityModule().trackedEntityInstances()
                    .withTrackedEntityAttributeValues().uid(toTeiUid).blockingGet()
                RelationshipViewModel.create(
                    relationship,
                    relationshipType,
                    direction,
                    relationshipTEIUid,
                    getTrackedEntityAttributesForRelationship(fromTei, programUid),
                    getTrackedEntityAttributesForRelationship(toTei, programUid),
                    fromTei.geometry(),
                    toTei.geometry(),
                    fromTei.profilePicturePath(d2, programUid),
                    toTei.profilePicturePath(d2, programUid),
                    getTeiDefaultRes(fromTei),
                    getTeiDefaultRes(toTei)
                )
            }
        }
    }

    private fun getTrackedEntityAttributesForRelationship(
        tei: TrackedEntityInstance,
        programUid: String?
    ): List<TrackedEntityAttributeValue?>? {
        var values: List<TrackedEntityAttributeValue?>
        val attributeUids: MutableList<String> = ArrayList()
        val programTrackedEntityAttributes = d2.programModule().programTrackedEntityAttributes()
            .byProgram().eq(programUid)
            .byDisplayInList().isTrue
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
        for (programAttribute in programTrackedEntityAttributes) {
            attributeUids.add(programAttribute.trackedEntityAttribute()!!.uid())
        }
        values = d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityInstance().eq(tei.uid())
            .byTrackedEntityAttribute().`in`(attributeUids).blockingGet()
        if (values.isEmpty()) {
            attributeUids.clear()
            val typeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid().eq(tei.trackedEntityType())
                .byDisplayInList().isTrue
                .blockingGet()
            for (typeAttribute in typeAttributes) {
                attributeUids.add(typeAttribute.trackedEntityAttribute()!!.uid())
            }
            values = d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(tei.uid())
                .byTrackedEntityAttribute().`in`(attributeUids).blockingGet()
        }
        return values
    }

    private fun getTeiDefaultRes(tei: TrackedEntityInstance): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet()
        return resources.getObjectStyleDrawableResource(
            teiType.style().icon(),
            R.drawable.photo_temp_gray
        )
    }
}

sealed class RelationshipConfiguration
data class TrackerRelationshipConfiguration(
    val enrollmentUid: String,
    val teiUid: String
) : RelationshipConfiguration()

data class EventRelationshipConfiguration(
    val stageUid: String,
    val eventUid: String
) : RelationshipConfiguration()