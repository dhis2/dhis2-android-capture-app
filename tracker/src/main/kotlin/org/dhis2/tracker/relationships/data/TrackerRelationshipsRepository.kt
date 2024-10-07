package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.data.RelationshipDirection
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.R
import org.dhis2.tracker.extensions.profilePicturePath
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import java.util.Date

class TrackerRelationshipsRepository(
    private val d2: D2,
    private val teiAttributesProvider: TeiAttributesProvider,
    private val resources: ResourceManager,
    private val metadataIconProvider: MetadataIconProvider,
    private val teiUid: String,
    private val enrollmentUid: String,
) : RelationshipsRepository {
    override fun getRelationshipTypes(): Flow<List<Pair<RelationshipType, String>>> {
        val teTypeUid = d2.trackedEntityModule().trackedEntityInstances()
            .uid(teiUid)
            .blockingGet()?.trackedEntityType() ?: return flowOf(emptyList())

        return flowOf(d2.relationshipModule()
            .relationshipTypes()
            .withConstraints()
            .byAvailableForTrackedEntityInstance(teiUid)
            .blockingGet().mapNotNull { relationshipType ->
                val secondaryTeTypeUid = when {
                    relationshipType.fromConstraint()?.trackedEntityType()
                        ?.uid() == teTypeUid ->
                        relationshipType.toConstraint()?.trackedEntityType()?.uid()

                    relationshipType.bidirectional() == true && relationshipType.toConstraint()
                        ?.trackedEntityType()?.uid() == teTypeUid ->
                        relationshipType.fromConstraint()?.trackedEntityType()?.uid()

                    else -> null
                }
                secondaryTeTypeUid?.let {
                    Pair(relationshipType, secondaryTeTypeUid)
                }
            }
        )
    }

    override fun getRelationships(): Flow<List<RelationshipViewModel>> {
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
                val relationshipType =
                    d2.relationshipModule().relationshipTypes().withConstraints()
                        .uid(relationship.relationshipType())
                        .blockingGet() ?: return@mapNotNull null
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
                            relationshipType.fromConstraint()
                        )
                        fromProfilePic = tei?.profilePicturePath(d2, programUid)
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
                                relationshipType.toConstraint()
                            )
                            toProfilePic = toTei?.profilePicturePath(d2, programUid)
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
                            toValues = getEventValuesForRelationship(toEvent?.uid())
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
                            relationshipType.toConstraint()
                        )
                        toProfilePic = tei?.profilePicturePath(d2, programUid)
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
                                relationshipType.fromConstraint()
                            )
                            fromProfilePic = fromTei?.profilePicturePath(d2, programUid)
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
                            fromValues = getEventValuesForRelationship(fromEvent?.uid())
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

                RelationshipViewModel(
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
                    getOwnerColor(relationshipOwnerUid, relationshipOwnerType),
                    canBoOpened,
                    toLastUpdated,
                    fromLastUpdated,
                    toDescription,
                    fromDescription,
                )
            }
        )
    }

    private fun getTeiAttributesForRelationship(
        teiUid: String?,
        relationshipConstraint: RelationshipConstraint?,
    ): List<Pair<String, String>> {
        //Get list of ordered attributes
        val trackedEntityAttributesUids = when {

            //When there are  attributes defined in the constraint
            !relationshipConstraint?.trackerDataView()?.attributes().isNullOrEmpty() -> {
                relationshipConstraint?.trackerDataView()?.attributes()
            }

            //If there is a program defined in the constraint
            relationshipConstraint?.program() != null -> {
                val programUid = relationshipConstraint.program()!!.uid()
                d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(programUid)
                    .byDisplayInList().isTrue
                    .blockingGet().mapNotNull {
                        it.trackedEntityAttribute()?.uid()
                    }
            }

            //If there is no program then we get the trackedEntity type attributes
            relationshipConstraint?.trackedEntityType()?.uid() != null -> {
                val teiTypeUid = relationshipConstraint.trackedEntityType()?.uid()
                d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(teiTypeUid)
                    .byDisplayInList().isTrue.blockingGet().mapNotNull {
                        it.trackedEntityAttribute()?.uid()
                    }
            }

            else -> {
                listOf()
            }

        }

        //Get a list of Pair<DisplayName, value>
        val attributes = trackedEntityAttributesUids!!.mapNotNull { attributeUid ->
            val fieldName = d2.trackedEntityModule().trackedEntityAttributes()
                .uid(attributeUid).blockingGet()
                ?.displayFormName()

            val value = d2.trackedEntityModule().trackedEntityAttributeValues()
                .value(attributeUid, teiUid!!).blockingGet()?.userFriendlyValue(d2)
            if (fieldName != null && value != null) {
                Pair(fieldName, value)
            } else {
                null
            }
        }

        return attributes.ifEmpty {
            val teiTypeUid = relationshipConstraint?.trackedEntityType()?.uid()
            val teiTypeName = d2.trackedEntityModule().trackedEntityTypes()
                .uid(teiTypeUid).blockingGet()?.name() ?: ""
            listOf(Pair("uid", teiTypeName))
        }
    }

    private fun getTeiDefaultRes(tei: TrackedEntityInstance?): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes()
                .uid(tei?.trackedEntityType())
                .blockingGet()
        return getTeiTypeDefaultRes(teiType?.uid())
    }

    private fun getTeiTypeDefaultRes(teiTypeUid: String?): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes().uid(teiTypeUid).blockingGet()
        return resources.getObjectStyleDrawableResource(
            teiType?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    private fun orgUnitInScope(orgUnitUid: String?): Boolean {
        return orgUnitUid?.let {
            val inCaptureScope = d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .uid(orgUnitUid)
                .blockingExists()
            val inSearchScope = d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .uid(orgUnitUid)
                .blockingExists()
            inCaptureScope || inSearchScope
        } ?: false
    }

    private fun getEventValuesForRelationship(eventUid: String?): List<Pair<String, String>> {
        val event =
            d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).blockingGet()
        val deFromEvent = d2.programModule().programStageDataElements()
            .byProgramStage().eq(event?.programStage())
            .byDisplayInReports().isTrue.blockingGetUids()

        val valuesFromEvent = event?.trackedEntityDataValues()?.mapNotNull {
            if (!deFromEvent.contains(it.dataElement())) return@mapNotNull null
            val formName = d2.dataElementModule().dataElements().uid(it.dataElement()).blockingGet()
                ?.displayName()
            val value = it.userFriendlyValue(d2)
            if (formName != null && value != null) {
                Pair(formName, value)
            } else {
                null
            }
        } ?: emptyList()

        return if (valuesFromEvent.isNotEmpty()) {
            valuesFromEvent
        } else {
            val stage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
            listOf(Pair("displayName", stage?.displayName() ?: event?.uid() ?: ""))
        }
    }

    private fun getEventDefaultRes(event: Event?): Int {
        val stage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
        val program = d2.programModule().programs().uid(event?.program()).blockingGet()
        return resources.getObjectStyleDrawableResource(
            stage?.style()?.icon() ?: program?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    private fun getOwnerColor(
        uid: String,
        relationshipOwnerType: RelationshipOwnerType,
    ): MetadataIconData {
        return when (relationshipOwnerType) {
            RelationshipOwnerType.EVENT -> {
                val event = d2.eventModule().events().uid(uid).blockingGet()
                val program = d2.programModule().programs().uid(event?.program()).blockingGet()
                if (program?.programType() == ProgramType.WITHOUT_REGISTRATION) {
                    metadataIconProvider.invoke(program.style(), SurfaceColor.Primary)
                } else {
                    val programStage =
                        d2.programModule().programStages().uid(event?.programStage()).blockingGet()
                    metadataIconProvider(programStage!!.style(), SurfaceColor.Primary)
                }
            }

            RelationshipOwnerType.TEI -> {
                val tei = d2.trackedEntityModule().trackedEntityInstances()
                    .uid(uid).blockingGet()
                val teType = d2.trackedEntityModule().trackedEntityTypes()
                    .uid(tei?.trackedEntityType()).blockingGet()
                return metadataIconProvider(teType!!.style(), SurfaceColor.Primary)
            }
        }
    }

    private fun getStage(programStageUid: String): ProgramStage? {
        return d2.programModule().programStages()
            .uid(programStageUid)
            .blockingGet()
    }
}