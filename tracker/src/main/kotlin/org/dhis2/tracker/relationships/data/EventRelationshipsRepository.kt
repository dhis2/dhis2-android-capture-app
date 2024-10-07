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
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemEvent
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class EventRelationshipsRepository(
    private val d2: D2,
    private val teiAttributesProvider: TeiAttributesProvider,
    private val resources: ResourceManager,
    private val metadataIconProvider: MetadataIconProvider,
    private val eventUid: String,
) : RelationshipsRepository {
    override fun getRelationshipTypes(): Flow<List<Pair<RelationshipType, String>>> {
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
                                ),
                            getEventValuesForRelationship(eventUid),
                        )
                    } else {
                        Pair(
                            getEventValuesForRelationship(eventUid),
                            getTeiAttributesForRelationship(
                                relationshipOwnerUid,
                                relationshipType.toConstraint(),
                            ),
                        )
                    }

                val (fromProfilePic, toProfilePic) =
                    if (direction == RelationshipDirection.FROM) {
                        Pair(
                            tei?.profilePicturePath(d2, null),
                            null,
                        )
                    } else {
                        Pair(
                            null,
                            tei?.profilePicturePath(d2, null),
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
                    getOwnerColor(relationshipOwnerUid, RelationshipOwnerType.TEI),
                    canBeOpened,
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

    private fun getEventDefaultRes(event: Event?): Int {
        val stage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
        val program = d2.programModule().programs().uid(event?.program()).blockingGet()
        return resources.getObjectStyleDrawableResource(
            stage?.style()?.icon() ?: program?.style()?.icon(),
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