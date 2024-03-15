package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.Single
import org.dhis2.R
import org.dhis2.bindings.profilePicturePath
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.data.RelationshipDirection
import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.ui.MetadataIconData
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.relationship.RelationshipEntityType
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemEvent
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class RelationshipRepositoryImpl(
    private val d2: D2,
    private val config: RelationshipConfiguration,
    private val resources: ResourceManager,
    private val teiAttributesProvider: TeiAttributesProvider,
    private val metadataIconProvider: MetadataIconProvider,
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
        // TODO: Limit link to only TEI
        val teTypeUid = d2.trackedEntityModule().trackedEntityInstances()
            .uid((config as TrackerRelationshipConfiguration).teiUid)
            .blockingGet()?.trackedEntityType() ?: return Single.just(emptyList())

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
                    secondaryTeTypeUid?.let {
                        Pair(relationshipType, secondaryTeTypeUid)
                    }
                }
            }
    }

    private fun stageRelationshipTypes(): Single<List<Pair<RelationshipType, String>>> {
        // TODO: Limit links to TEI
        val event = d2.eventModule().events().uid(
            (config as EventRelationshipConfiguration).eventUid,
        ).blockingGet()
        val programStageUid = event?.programStage() ?: ""
        val programUid = event?.program() ?: ""
        return d2.relationshipModule().relationshipTypes()
            .withConstraints()
            .byAvailableForEvent(event?.uid() ?: "")
            .get().map { relationshipTypes ->
                relationshipTypes.mapNotNull { relationshipType ->
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
                    secondaryUid?.let { Pair(relationshipType, secondaryUid) }
                }
            }
    }

    fun eventRelationships(): Single<List<RelationshipViewModel>> {
        val eventUid = (config as EventRelationshipConfiguration).eventUid
        return Single.fromCallable {
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().event(
                    RelationshipItemEvent.builder().event(eventUid).build(),
                ).build(),
            ).mapNotNull { relationship ->
                val relationshipType =
                    d2.relationshipModule().relationshipTypes()
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
                            getTeiAttributesForRelationship(relationshipOwnerUid),
                            getEventValuesForRelationship(eventUid),
                        )
                    } else {
                        Pair(
                            getEventValuesForRelationship(eventUid),
                            getTeiAttributesForRelationship(relationshipOwnerUid),
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
                )
            }
        }
    }

    fun enrollmentRelationships(): Single<List<RelationshipViewModel>> {
        val teiUid = (config as TrackerRelationshipConfiguration).teiUid
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(teiUid).blockingGet()
        val programUid = d2.enrollmentModule().enrollments()
            .uid(config.enrollmentUid).blockingGet()?.program()
        return Single.fromCallable {
            d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().trackedEntityInstance(
                    RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teiUid)
                        .build(),
                ).build(),
            ).mapNotNull { relationship ->
                val relationshipType =
                    d2.relationshipModule().relationshipTypes()
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

                when (teiUid) {
                    relationship.from()?.trackedEntityInstance()?.trackedEntityInstance() -> {
                        direction = RelationshipDirection.TO
                        fromGeometry = tei?.geometry()
                        fromValues = getTeiAttributesForRelationship(teiUid)
                        fromProfilePic = tei?.profilePicturePath(d2, programUid)
                        fromDefaultPicRes = getTeiDefaultRes(tei)
                        if (relationship.to()?.trackedEntityInstance() != null) {
                            relationshipOwnerType = RelationshipOwnerType.TEI
                            relationshipOwnerUid =
                                relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()
                            val toTei = d2.trackedEntityModule().trackedEntityInstances()
                                .uid(relationshipOwnerUid).blockingGet()
                            toGeometry = toTei?.geometry()
                            toValues = getTeiAttributesForRelationship(toTei?.uid())
                            toProfilePic = toTei?.profilePicturePath(d2, programUid)
                            toDefaultPicRes = getTeiDefaultRes(toTei)
                            canBoOpened = toTei?.syncState() != State.RELATIONSHIP &&
                                orgUnitInScope(toTei?.organisationUnit())
                        } else {
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
                        }
                    }
                    relationship.to()?.trackedEntityInstance()?.trackedEntityInstance() -> {
                        direction = RelationshipDirection.FROM
                        toGeometry = tei?.geometry()
                        toValues = getTeiAttributesForRelationship(teiUid)
                        toProfilePic = tei?.profilePicturePath(d2, programUid)
                        toDefaultPicRes = getTeiDefaultRes(tei)
                        if (relationship.from()?.trackedEntityInstance() != null) {
                            relationshipOwnerType = RelationshipOwnerType.TEI
                            relationshipOwnerUid =
                                relationship.from()?.trackedEntityInstance()
                                    ?.trackedEntityInstance()
                            val fromTei = d2.trackedEntityModule().trackedEntityInstances()
                                .uid(relationshipOwnerUid).blockingGet()
                            fromGeometry = fromTei?.geometry()
                            fromValues = getTeiAttributesForRelationship(fromTei?.uid())
                            fromProfilePic = fromTei?.profilePicturePath(d2, programUid)
                            fromDefaultPicRes = getTeiDefaultRes(fromTei)
                            canBoOpened = fromTei?.syncState() != State.RELATIONSHIP &&
                                orgUnitInScope(fromTei?.organisationUnit())
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
                )
            }
        }
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

    private fun getOwnerColor(uid: String, relationshipOwnerType: RelationshipOwnerType): MetadataIconData {
        return when (relationshipOwnerType) {
            RelationshipOwnerType.EVENT -> {
                val event = d2.eventModule().events().uid(uid).blockingGet()
                val program = d2.programModule().programs().uid(event?.program()).blockingGet()
                if (program?.programType() == ProgramType.WITHOUT_REGISTRATION) {
                    metadataIconProvider.invoke(program.style())
                } else {
                    val programStage =
                        d2.programModule().programStages().uid(event?.programStage()).blockingGet()
                    metadataIconProvider(programStage!!.style())
                }
            }
            RelationshipOwnerType.TEI -> {
                val tei = d2.trackedEntityModule().trackedEntityInstances()
                    .uid(uid).blockingGet()
                val teType = d2.trackedEntityModule().trackedEntityTypes()
                    .uid(tei?.trackedEntityType()).blockingGet()
                return metadataIconProvider(teType!!.style())
            }
        }
    }

    private fun getTeiAttributesForRelationship(teiUid: String?): List<Pair<String, String>> {
        val teiTypeUid = d2.trackedEntityModule()
            .trackedEntityInstances().uid(teiUid).blockingGet()?.trackedEntityType()

        val attrValuesFromType = mutableListOf<Pair<String, String>>()
        teiUid?.let {
            teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(teiTypeUid, it)
                .mapNotNull { attributeValue ->
                    val fieldName = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(attributeValue.trackedEntityAttribute()).blockingGet()
                        ?.displayFormName()
                    val value = attributeValue.userFriendlyValue(d2)
                    if (fieldName != null && value != null) {
                        attrValuesFromType.add(Pair(fieldName, value))
                    } else {
                        null
                    }
                }
        }

        val attrValueFromProgramTrackedEntityAttribute = mutableListOf<Pair<String, String>>()
        val teiTypeName = d2.trackedEntityModule().trackedEntityTypes()
            .uid(teiTypeUid).blockingGet()?.name() ?: ""

        if (attrValuesFromType.isEmpty()) {
            teiUid?.let {
                teiAttributesProvider.getValuesFromProgramTrackedEntityAttributes(teiTypeUid, it)
                    .mapNotNull { attributeValue ->
                        val fieldName = d2.trackedEntityModule().trackedEntityAttributes()
                            .uid(attributeValue.trackedEntityAttribute())
                            .blockingGet()?.displayFormName()
                        val value = attributeValue.userFriendlyValue(d2)
                        if (fieldName != null && value != null) {
                            attrValueFromProgramTrackedEntityAttribute.add(Pair(fieldName, value))
                        } else {
                            null
                        }
                    }
            }
        }

        return when {
            attrValuesFromType.isNotEmpty() -> {
                attrValuesFromType
            }
            attrValuesFromType.isEmpty() -> {
                attrValueFromProgramTrackedEntityAttribute
            }
            else -> {
                listOf(Pair("uid", teiTypeName))
            }
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

    override fun getTeiTypeDefaultRes(teiTypeUid: String?): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes().uid(teiTypeUid).blockingGet()
        return resources.getObjectStyleDrawableResource(
            teiType?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    override fun getEventProgram(eventUid: String?): String {
        return d2.eventModule().events().uid(eventUid).blockingGet()?.program() ?: ""
    }

    private fun getEventDefaultRes(event: Event?): Int {
        val stage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
        val program = d2.programModule().programs().uid(event?.program()).blockingGet()
        return resources.getObjectStyleDrawableResource(
            stage?.style()?.icon() ?: program?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }
}

sealed class RelationshipConfiguration
data class TrackerRelationshipConfiguration(
    val enrollmentUid: String,
    val teiUid: String,
) : RelationshipConfiguration()

data class EventRelationshipConfiguration(
    val eventUid: String,
) : RelationshipConfiguration()
